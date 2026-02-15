import request from './request'

// 查询积分余额
export function getPointsBalance() {
  return request({
    url: '/points/balance',
    method: 'get'
  })
}

// 查询积分流水记录
export function getPointsRecords(params) {
  return request({
    url: '/points/records',
    method: 'get',
    params
  })
}

// 消费积分
export function spendPoints(data) {
  return request({
    url: '/points/spend',
    method: 'post',
    data
  })
}
