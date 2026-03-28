import request from './request'
import type { ApiResponse } from './request'

/** 带超时的 fetch 封装 */
async function fetchWithTimeout(url: string, timeoutMs = 30000): Promise<Response> {
  const controller = new AbortController()
  const timeoutId = setTimeout(() => controller.abort(), timeoutMs)
  try {
    const response = await fetch(url, { signal: controller.signal })
    return response
  } catch (error) {
    if (error instanceof DOMException && error.name === 'AbortError') {
      throw new Error('请求超时，请稍后重试')
    }
    throw error
  } finally {
    clearTimeout(timeoutId)
  }
}

// 文件信息接口
export interface FileInfo {
  id: string
  matterId: string
  clientId: number
  fileName: string
  fileSize: number
  fileType?: string  // MIME类型
  fileCategory: string
  description?: string
  fileSource?: string  // 文件来源：PUSHED（系统推送）、CLIENT_UPLOAD（客户上传）
  uploadedAt: string  // 与后端DTO字段名一致
  status: string
}

// 文件来源常量
export const FILE_SOURCE_PUSHED = 'PUSHED'  // 系统推送
export const FILE_SOURCE_CLIENT_UPLOAD = 'CLIENT_UPLOAD'  // 客户上传

// 上传文件（支持进度回调）
export function uploadFile(
  matterId: string,
  clientId: number,
  token: string,
  file: File,
  category: string = 'OTHER',
  description?: string,
  onProgress?: (progress: number) => void
): Promise<ApiResponse<FileInfo>> {
  const formData = new FormData()
  formData.append('matterId', matterId)
  formData.append('clientId', String(clientId))
  formData.append('token', token)
  formData.append('file', file)
  formData.append('fileCategory', category)
  if (description) {
    formData.append('description', description)
  }

  return request.post('/api/client/files/upload', formData, {
    headers: {
      'Content-Type': 'multipart/form-data',
    },
    onUploadProgress: (progressEvent: { loaded: number; total?: number }) => {
      if (onProgress && progressEvent.total) {
        const percent = Math.round((progressEvent.loaded * 100) / progressEvent.total)
        onProgress(percent)
      }
    },
  })
}

// 获取文件列表
export function getFileList(matterId: string, token: string): Promise<ApiResponse<FileInfo[]>> {
  return request.get('/api/client/files', {
    params: { matterId, token },
  })
}

/**
 * 下载文件
 * 使用fetch API下载，避免token直接暴露在浏览器地址栏和历史记录中
 * 注意：token仍会出现在请求URL中（后端API设计限制），但不会被浏览器记录
 */
export async function downloadFile(fileId: string, matterId: string, token: string, fileName?: string): Promise<void> {
  const url = `/api/client/files/${fileId}/download?matterId=${matterId}&token=${encodeURIComponent(token)}`
  
  try {
    const response = await fetchWithTimeout(url, 60000)
    
    if (!response.ok) {
      throw new Error(`下载失败: ${response.status} ${response.statusText}`)
    }
    
    const blob = await response.blob()
    
    // 尝试从Content-Disposition获取文件名
    const contentDisposition = response.headers.get('Content-Disposition')
    let downloadFileName = fileName || 'download'
    
    if (contentDisposition) {
      // 匹配 filename*=UTF-8''xxx 或 filename="xxx" 格式
      const utf8Match = contentDisposition.match(/filename\*=UTF-8''([^;]+)/i)
      const normalMatch = contentDisposition.match(/filename[^;=\n]*=((['"]).*?\2|[^;\n]*)/)
      
      if (utf8Match) {
        downloadFileName = decodeURIComponent(utf8Match[1])
      } else if (normalMatch) {
        downloadFileName = normalMatch[1].replace(/['"]/g, '')
      }
    }
    
    // 创建下载链接
    const downloadUrl = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = downloadUrl
    link.download = downloadFileName
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.URL.revokeObjectURL(downloadUrl)
  } catch (error) {
    // 不再降级到window.open，避免token暴露在浏览器地址栏和历史记录中
    const errorMessage = error instanceof Error ? error.message : '文件下载失败'
    throw new Error(`下载失败: ${errorMessage}`)
  }
}

/**
 * 获取文件预览URL
 * 注意：此URL包含token，仅用于页面内嵌显示（img/iframe），不应暴露给用户
 * 预览链接不会出现在浏览器地址栏，相对安全
 */
export function previewFile(fileId: string, matterId: string, token: string): string {
  return `/api/client/files/${fileId}/preview?matterId=${matterId}&token=${encodeURIComponent(token)}`
}

/**
 * 获取文件预览Blob（更安全的方式，不暴露URL）
 * 适用于需要更高安全性的场景
 */
export async function getPreviewBlob(fileId: string, matterId: string, token: string): Promise<string> {
  const url = `/api/client/files/${fileId}/preview?matterId=${matterId}&token=${encodeURIComponent(token)}`
  
  const response = await fetchWithTimeout(url, 30000)
  
  if (!response.ok) {
    throw new Error(`预览失败: ${response.status} ${response.statusText}`)
  }
  
  const blob = await response.blob()
  return window.URL.createObjectURL(blob)
}

// 删除文件
export function deleteFile(fileId: string, matterId: string, token: string): Promise<ApiResponse<void>> {
  return request.delete(`/api/client/files/${fileId}`, {
    params: { matterId, token },
  })
}
