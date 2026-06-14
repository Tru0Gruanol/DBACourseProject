import { ElMessage } from 'element-plus'

/**
 * 安全包装异步操作，自动处理 loading 状态重置。
 *
 * 通过 Axios 拦截器已 toast 网络/业务错误，此工具仅负责：
 * 1. 无论成功失败都执行 finally 重置 loading
 * 2. 用户主动取消操作（ElMessageBox cancel/close）不弹错误
 * 3. 返回 null 表示操作失败/取消，方便调用方判断
 *
 * @param {Function} fn          异步操作函数
 * @param {import('vue').Ref<boolean>} loadingRef loading 状态的 ref
 * @returns {Promise<*>} 操作结果，失败返回 null
 *
 * @example
 * const result = await safeAsync(() => getTeachers(), loading)
 * if (result) { teachers.value = result }
 */
export async function safeAsync(fn, loadingRef) {
  try {
    return await fn()
  } catch (e) {
    // Axios 拦截器已 toast，此仅吞掉异常防止未捕获的 rejection
    // 用户点 ElMessageBox 取消时 e === 'cancel' / 'close'，无需处理
    return null
  } finally {
    if (loadingRef) loadingRef.value = false
  }
}

/**
 * 安全执行带确认框的异步操作。
 * 用户取消确认框时不弹错误提示。
 *
 * @param {Function} fn          异步操作函数
 * @param {import('vue').Ref<boolean>} loadingRef loading 状态的 ref
 * @returns {Promise<*>} 操作结果，失败或取消返回 null
 */
export async function safeConfirmAsync(fn, loadingRef) {
  try {
    return await fn()
  } catch (e) {
    // 'cancel' / 'close' = 用户在 ElMessageBox 点取消，不是错误
    if (e !== 'cancel' && e !== 'close') {
      // Axios 拦截器已 toast 网络错误
    }
    return null
  } finally {
    if (loadingRef) loadingRef.value = false
  }
}
