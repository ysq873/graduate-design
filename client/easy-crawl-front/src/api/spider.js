import { axios } from '@/utils/request'

export function searchSpider (data) {
  return axios({
    url: '/configurable-spider/search',
    method: 'post',
    data
  })
}

export function addSpider (data) {
  return axios({
    url: '/configurable-spider',
    method: 'post',
    data
  })
}

/**
 * 跟新爬虫
 */
export function updateSpider (data) {
  return axios({
    url: '/configurable-spider',
    method: 'put',
    data
  })
}

/**
 * 设置爬虫允许参数
 */
export function configSpiderSettings (data) {
  return axios({
    url: '/configurable-spider/settings',
    method: 'put',
    data
  })
}

// /**
//  * 配置代理
//  */
// export function configSpiderProxy (data) {
//   return axios({
//     url: '/configurable-spider/config-proxy',
//     method: 'post',
//     data
//   })
// }

export function exportSpider (id) {
  return axios({
    url: `configurable-spider/export/${id}`,
    method: 'post'
  })
}

/**
 * 删除爬虫
 * @param {*} id 爬虫id
 */
export function deleteSpider (id) {
  return axios({
    url: `configurable-spider/${id}`,
    method: 'delete'
  })
}

/**
 * 测试爬虫页面
 */
export function testPage (url, isDynamic) {
  return axios({
    url: 'configurable-spider/test-page',
    method: 'post',
    data: {
      url,
      isDynamic
    }
  })
}

/**
 * 测试xpath
 */
export function testXpath (url, xpath, isDynamic) {
  return axios({
    url: 'configurable-spider/test-xpath',
    method: 'post',
    data: {
      url,
      xpath,
      isDynamic
    }
  })
}

/**
 * 联想xpath
 */
export function guessXpath (url, content, isDynamic) {
  return axios({
    url: 'configurable-spider/guess-xpath',
    method: 'post',
    data: {
      url,
      content,
      isDynamic
    }
  })
}

/**
 * 测试正则
 */
export function testRegex (url, regex, isDynamic) {
  return axios({
    url: 'configurable-spider/test-regex',
    method: 'post',
    data: {
      url, regex, isDynamic
    }
  })
}

/**
 * 测试内容页字段xpath
 * @param entryUrl 入口页链接
 * @param contentXpath 正文页XPATH
 * @param xpath 正文页字段xpath
 */
export function testContentXpath (entryUrl, contentXpath, xpath, isDynamic) {
  return axios({
    url: 'configurable-spider/test-content-xpath',
    method: 'post',
    data: {
      entryUrl, contentXpath, xpath, isDynamic
    }
  })
}
