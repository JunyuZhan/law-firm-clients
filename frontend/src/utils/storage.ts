/**
 * LocalStorage 封装工具
 * 提供统一的存储接口，处理异常情况
 */

import logger from './logger'

/**
 * 安全获取 localStorage 中的值
 * @param key 存储键
 * @param defaultValue 默认值
 * @returns 存储的值或默认值
 */
export function getItem<T = string>(key: string, defaultValue: T | null = null): T | null {
  try {
    const item = localStorage.getItem(key)
    if (item === null) {
      return defaultValue
    }
    return item as T
  } catch (error) {
    logger.warn(`读取 localStorage 失败: key=${key}`, error)
    return defaultValue
  }
}

/**
 * 安全获取并解析 JSON 对象
 * @param key 存储键
 * @param defaultValue 默认值
 * @returns 解析后的对象或默认值
 */
export function getJSON<T>(key: string, defaultValue: T | null = null): T | null {
  try {
    const item = localStorage.getItem(key)
    if (item === null) {
      return defaultValue
    }
    return JSON.parse(item) as T
  } catch (error) {
    logger.warn(`解析 localStorage JSON 失败: key=${key}`, error)
    return defaultValue
  }
}

/**
 * 安全设置 localStorage 值
 * @param key 存储键
 * @param value 要存储的值
 * @returns 是否成功
 */
export function setItem(key: string, value: string): boolean {
  try {
    localStorage.setItem(key, value)
    return true
  } catch (error) {
    logger.error(`写入 localStorage 失败: key=${key}`, error)
    return false
  }
}

/**
 * 安全存储 JSON 对象
 * @param key 存储键
 * @param value 要存储的对象
 * @returns 是否成功
 */
export function setJSON<T>(key: string, value: T): boolean {
  try {
    localStorage.setItem(key, JSON.stringify(value))
    return true
  } catch (error) {
    logger.error(`写入 localStorage JSON 失败: key=${key}`, error)
    return false
  }
}

/**
 * 安全删除 localStorage 值
 * @param key 存储键
 * @returns 是否成功
 */
export function removeItem(key: string): boolean {
  try {
    localStorage.removeItem(key)
    return true
  } catch (error) {
    logger.warn(`删除 localStorage 失败: key=${key}`, error)
    return false
  }
}

/**
 * 批量删除 localStorage 值
 * @param keys 存储键数组
 * @returns 成功删除的数量
 */
export function removeItems(keys: string[]): number {
  let count = 0
  for (const key of keys) {
    if (removeItem(key)) {
      count++
    }
  }
  return count
}

/**
 * 清除所有 localStorage（谨慎使用）
 * @returns 是否成功
 */
export function clear(): boolean {
  try {
    localStorage.clear()
    return true
  } catch (error) {
    logger.error('清除 localStorage 失败', error)
    return false
  }
}

/**
 * 检查 localStorage 是否可用
 * @returns 是否可用
 */
export function isAvailable(): boolean {
  try {
    const testKey = '__storage_test__'
    localStorage.setItem(testKey, testKey)
    localStorage.removeItem(testKey)
    return true
  } catch {
    return false
  }
}
