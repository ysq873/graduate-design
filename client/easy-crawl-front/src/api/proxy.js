import { axios } from '@/utils/request'

// 创建代理
export function addProxy (data) {
  return axios({
    url: '/proxy-channel',
    method: 'post',
    data
  })
}

// 代理列表
export function listProxy (data) {
  return axios({
    url: '/proxy-channel/list',
    method: 'post',
    data
  })
}
