import axios from 'axios'
import { ElMessage } from 'element-plus'

const request = axios.create({
  baseURL: 'http://localhost:8080/api',
  timeout: 15000,
})

request.interceptors.response.use(
  response => {
    const data = response.data
    // 后端当前直接返回纯字符串："xxx成功" 或 "xxx失败：原因..."
    // HTTP 状态码均为 200，前端需自行判断字符串内容
    if (typeof data === 'string' && (data.includes('失败') || data.includes('已存在') || data.includes('不能'))) {
      ElMessage.error(data)
      return Promise.reject(new Error(data))
    }
    return data
  },
  error => {
    let message = '请求失败'
    const data = error.response?.data
    if (data) {
      if (typeof data === 'string') {
        message = data
      } else if (data.message) {
        // Spring Boot 默认错误 JSON: { message: "...", error: "..." }
        message = data.message
      } else if (data.error) {
        message = data.error
      } else {
        // 最后一个兜底：序列化为字符串
        message = JSON.stringify(data)
      }
    } else if (error.message) {
      message = error.message
    }
    ElMessage.error(message)
    return Promise.reject(error)
  }
)

export default request