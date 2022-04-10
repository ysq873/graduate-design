import { axios } from '@/utils/request'

//添加任务
export function addJob(data) {
  return axios({
    url: '/job',
    method: 'post',
    data
  })
}

//获取任务
export function getJob(id) {
  return axios({
    url: `/job/${id}`,
    method: 'get'
  })
}

//结束任务
export function stopJob(id) {
  return axios({
    url: `/job/stop/${id}`,
    method: 'post'
  })
}

//暂停任务
export function pauseJob(id) {
  return axios({
    url: `/job/pause/${id}`,
    method: 'post'
  })
}

//重启任务
export function restartJob(id) {
  return axios({
    url: `/job/restart/${id}`,
    method: 'post'
  })
}

//分页查询任务
export function searchJob(data) {
  return axios({
    url: '/job/search',
    method: 'post',
    data
  })
}

export function searchJobRecord(data) {
  return axios({
    url: '/job-record/search',
    method: 'post',
    data
  })
}

//获取任务统计信息
export function getStatInfo(id) {
  return axios({
    url: `/job/get-stat-info/${id}`,
    method: 'get'
  })
}
