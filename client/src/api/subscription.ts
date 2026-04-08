import http from './auth'

export function getPlans() {
  return http.get('/subscription/plans')
}

export function getCurrentSubscription() {
  return http.get('/subscription/current')
}

export function getQuotaStatus() {
  return http.get('/subscription/quota')
}

export function upgradePlan(planCode: string, billingCycle: string = 'MONTHLY') {
  return http.post('/subscription/upgrade', { planCode, billingCycle })
}

export function cancelSubscription() {
  return http.post('/subscription/cancel')
}
