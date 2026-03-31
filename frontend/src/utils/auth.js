export function isLogin() {
  return !!localStorage.getItem('token')
}

export function getToken() {
  return localStorage.getItem('token')
}

export function removeToken() {
  localStorage.removeItem('token')
}