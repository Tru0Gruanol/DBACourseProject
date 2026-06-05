import axios from 'axios'
import { ElMessage } from 'element-plus'

const request = axios.create({
  baseURL: 'http://localhost:8080/api',
  timeout: 15000,
})

request.interceptors.response.use(
  response => response.data,
  error => {
    const message = error.response?.data || error.message || '请求失败'
    ElMessage.error(message)
    return Promise.reject(error)
  }
)

export default request