import { reactive, readonly } from 'vue'

const modal = reactive({ current: null })

function showModal(config) {
  return new Promise((resolve) => {
    modal.current = { ...config, resolve }
  })
}

function closeModal(result) {
  if (modal.current?.resolve) modal.current.resolve(result)
  modal.current = null
}

function confirm(message, title = '确认操作') {
  return showModal({
    title,
    message,
    showCancel: true,
    confirmText: '确认',
    cancelText: '取消',
  })
}

function alert(message, title = '提示') {
  return showModal({
    title,
    message,
    showCancel: false,
    confirmText: '知道了',
  })
}

export function useModal() {
  return { modal: readonly(modal), showModal, closeModal, confirm, alert }
}
