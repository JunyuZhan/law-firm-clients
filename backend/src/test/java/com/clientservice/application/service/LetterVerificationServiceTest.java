package com.clientservice.application.service;

import com.clientservice.application.dto.LetterVerificationReceiveDTO;
import com.clientservice.application.dto.LetterVerificationResultDTO;
import com.clientservice.common.exception.BusinessException;
import com.clientservice.domain.entity.LetterVerification;
import com.clientservice.infrastructure.persistence.mapper.LetterVerificationMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * LetterVerificationService 单元测试
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("LetterVerificationService 单元测试")
class LetterVerificationServiceTest {

    @Mock
    private LetterVerificationMapper letterVerificationMapper;

    @Mock
    private HttpServletRequest httpServletRequest;

    @InjectMocks
    private LetterVerificationService letterVerificationService;

    private LetterVerificationReceiveDTO receiveDTO;
    private LetterVerification letterVerification;

    @BeforeEach
    void setUp() {
        receiveDTO = LetterVerificationReceiveDTO.builder()
                .letterId(1001L)
                .applicationNo("LTR202603020001")
                .verificationCode("abc123def456")
                .letterType("ATTORNEY_LETTER")
                .letterTypeName("律师函")
                .targetUnit("某某公司")
                .lawyerNames("张三, 李四")
                .firmName("北京某某律师事务所")
                .matterName("合同纠纷项目")
                .approvedAt(LocalDateTime.now().minusDays(1))
                .printedAt(LocalDateTime.now().minusHours(12))
                .validUntil(LocalDateTime.now().plusMonths(3))
                .remark("测试备注")
                .build();

        letterVerification = LetterVerification.builder()
                .id(1L)
                .letterId(1001L)
                .applicationNo("LTR202603020001")
                .verificationCode("abc123def456")
                .letterType("ATTORNEY_LETTER")
                .letterTypeName("律师函")
                .targetUnit("某某公司")
                .lawyerNames("张三, 李四")
                .firmName("北京某某律师事务所")
                .matterName("合同纠纷项目")
                .approvedAt(LocalDateTime.now().minusDays(1))
                .printedAt(LocalDateTime.now().minusHours(12))
                .validUntil(LocalDateTime.now().plusMonths(3))
                .remark("测试备注")
                .verifyCount(0)
                .status(LetterVerification.STATUS_ACTIVE)
                .build();

        lenient().when(httpServletRequest.getRemoteAddr()).thenReturn("192.168.1.100");
    }

    @Nested
    @DisplayName("接收验证数据测试")
    class ReceiveVerificationDataTests {

        @Test
        @DisplayName("接收验证数据成功 - 新数据")
        void receiveVerificationData_NewData_ShouldSuccess() {
            when(letterVerificationMapper.selectByApplicationNo(anyString())).thenReturn(null);
            when(letterVerificationMapper.insert(any(LetterVerification.class))).thenReturn(1);

            assertDoesNotThrow(() -> letterVerificationService.receiveVerificationData(receiveDTO));

            verify(letterVerificationMapper, times(1)).insert(any(LetterVerification.class));
            verify(letterVerificationMapper, never()).updateById(any(LetterVerification.class));
        }

        @Test
        @DisplayName("接收验证数据成功 - 更新已存在数据")
        void receiveVerificationData_ExistingData_ShouldUpdate() {
            when(letterVerificationMapper.selectByApplicationNo(anyString())).thenReturn(letterVerification);
            when(letterVerificationMapper.updateById(any(LetterVerification.class))).thenReturn(1);

            assertDoesNotThrow(() -> letterVerificationService.receiveVerificationData(receiveDTO));

            verify(letterVerificationMapper, times(1)).updateById(any(LetterVerification.class));
            verify(letterVerificationMapper, never()).insert(any(LetterVerification.class));
        }

        @Test
        @DisplayName("接收空数据应该抛出异常")
        void receiveVerificationData_NullData_ShouldThrowException() {
            assertThrows(BusinessException.class, () -> letterVerificationService.receiveVerificationData(null));
        }
    }

    @Nested
    @DisplayName("验证函件测试")
    class VerifyLetterTests {

        @Test
        @DisplayName("验证函件成功 - 有效")
        void verifyLetter_Valid_ShouldReturnSuccess() {
            when(letterVerificationMapper.selectByApplicationNo(anyString())).thenReturn(letterVerification);
            doNothing().when(letterVerificationMapper).updateVerifyInfo(anyLong(), anyString());

            LetterVerificationResultDTO result = letterVerificationService.verifyLetter(
                    "LTR202603020001", "abc123def456", httpServletRequest);

            assertTrue(result.getValid());
            assertEquals(LetterVerificationResultDTO.VERIFY_STATUS_VALID, result.getVerifyStatus());
            assertEquals("验证通过，该函件真实有效", result.getStatusMessage());
            assertNotNull(result.getApplicationNo());
            verify(letterVerificationMapper, times(1)).updateVerifyInfo(anyLong(), anyString());
        }

        @Test
        @DisplayName("验证函件失败 - 验证码错误")
        void verifyLetter_InvalidCode_ShouldReturnInvalid() {
            when(letterVerificationMapper.selectByApplicationNo(anyString())).thenReturn(letterVerification);
            doNothing().when(letterVerificationMapper).updateVerifyInfo(anyLong(), anyString());

            LetterVerificationResultDTO result = letterVerificationService.verifyLetter(
                    "LTR202603020001", "wrong-code", httpServletRequest);

            assertFalse(result.getValid());
            assertEquals(LetterVerificationResultDTO.VERIFY_STATUS_INVALID, result.getVerifyStatus());
            assertTrue(result.getStatusMessage().contains("验证码错误"));
        }

        @Test
        @DisplayName("验证函件失败 - 已过期")
        void verifyLetter_Expired_ShouldReturnExpired() {
            letterVerification.setValidUntil(LocalDateTime.now().minusDays(1));
            when(letterVerificationMapper.selectByApplicationNo(anyString())).thenReturn(letterVerification);
            doNothing().when(letterVerificationMapper).updateVerifyInfo(anyLong(), anyString());

            LetterVerificationResultDTO result = letterVerificationService.verifyLetter(
                    "LTR202603020001", "abc123def456", httpServletRequest);

            assertFalse(result.getValid());
            assertEquals(LetterVerificationResultDTO.VERIFY_STATUS_EXPIRED, result.getVerifyStatus());
            assertEquals("该函件已过期", result.getStatusMessage());
        }

        @Test
        @DisplayName("验证函件失败 - 已撤销")
        void verifyLetter_Revoked_ShouldReturnRevoked() {
            letterVerification.setStatus(LetterVerification.STATUS_REVOKED);
            when(letterVerificationMapper.selectByApplicationNo(anyString())).thenReturn(letterVerification);
            doNothing().when(letterVerificationMapper).updateVerifyInfo(anyLong(), anyString());

            LetterVerificationResultDTO result = letterVerificationService.verifyLetter(
                    "LTR202603020001", "abc123def456", httpServletRequest);

            assertFalse(result.getValid());
            assertEquals(LetterVerificationResultDTO.VERIFY_STATUS_REVOKED, result.getVerifyStatus());
            assertEquals("该函件已被撤销", result.getStatusMessage());
        }

        @Test
        @DisplayName("验证函件失败 - 未找到")
        void verifyLetter_NotFound_ShouldReturnNotFound() {
            when(letterVerificationMapper.selectByApplicationNo(anyString())).thenReturn(null);

            LetterVerificationResultDTO result = letterVerificationService.verifyLetter(
                    "LTR999999999999", "any-code", httpServletRequest);

            assertFalse(result.getValid());
            assertEquals(LetterVerificationResultDTO.VERIFY_STATUS_NOT_FOUND, result.getVerifyStatus());
            assertEquals("未找到该函件信息", result.getStatusMessage());
        }

        @Test
        @DisplayName("验证函件失败 - 编号为空")
        void verifyLetter_EmptyApplicationNo_ShouldReturnNotFound() {
            LetterVerificationResultDTO result = letterVerificationService.verifyLetter(
                    "", "any-code", httpServletRequest);

            assertFalse(result.getValid());
            assertEquals(LetterVerificationResultDTO.VERIFY_STATUS_NOT_FOUND, result.getVerifyStatus());
        }

        @Test
        @DisplayName("验证函件失败 - 验证码为空")
        void verifyLetter_EmptyCode_ShouldReturnInvalid() {
            LetterVerificationResultDTO result = letterVerificationService.verifyLetter(
                    "LTR202603020001", "", httpServletRequest);

            assertFalse(result.getValid());
            assertEquals(LetterVerificationResultDTO.VERIFY_STATUS_INVALID, result.getVerifyStatus());
        }
    }

    @Nested
    @DisplayName("获取验证信息测试")
    class GetVerificationInfoTests {

        @Test
        @DisplayName("获取验证信息成功")
        void getVerificationInfo_ShouldReturnInfo() {
            when(letterVerificationMapper.selectByApplicationNo(anyString())).thenReturn(letterVerification);

            LetterVerificationResultDTO result = letterVerificationService.getVerificationInfo("LTR202603020001");

            assertTrue(result.getValid());
            assertEquals("LTR202603020001", result.getApplicationNo());
            assertEquals("律师函", result.getLetterTypeName());
            assertEquals("北京某某律师事务所", result.getFirmName());
        }

        @Test
        @DisplayName("获取不存在的验证信息")
        void getVerificationInfo_NotFound_ShouldReturnNotFound() {
            when(letterVerificationMapper.selectByApplicationNo(anyString())).thenReturn(null);

            LetterVerificationResultDTO result = letterVerificationService.getVerificationInfo("LTR999999999999");

            assertFalse(result.getValid());
            assertEquals(LetterVerificationResultDTO.VERIFY_STATUS_NOT_FOUND, result.getVerifyStatus());
        }
    }

    @Nested
    @DisplayName("撤销验证测试")
    class RevokeVerificationTests {

        @Test
        @DisplayName("撤销验证成功")
        void revokeVerification_ShouldSuccess() {
            when(letterVerificationMapper.selectByLetterId(anyLong())).thenReturn(letterVerification);
            doNothing().when(letterVerificationMapper).revokeByLetterId(anyLong());

            assertDoesNotThrow(() -> letterVerificationService.revokeVerification(1001L));

            verify(letterVerificationMapper, times(1)).revokeByLetterId(1001L);
        }

        @Test
        @DisplayName("撤销不存在的函件应该抛出异常")
        void revokeVerification_NotFound_ShouldThrowException() {
            when(letterVerificationMapper.selectByLetterId(anyLong())).thenReturn(null);

            assertThrows(BusinessException.class, () -> letterVerificationService.revokeVerification(9999L));
        }

        @Test
        @DisplayName("撤销时函件ID为空应该抛出异常")
        void revokeVerification_NullId_ShouldThrowException() {
            assertThrows(BusinessException.class, () -> letterVerificationService.revokeVerification(null));
        }
    }
}
