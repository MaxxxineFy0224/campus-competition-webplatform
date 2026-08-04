import { reactive, readonly } from 'vue'

let toastId = 0
const toasts = reactive([])

function addToast(message, type = 'info', duration = 3000) {
  const id = ++toastId
  toasts.push({ id, message, type })
  if (duration > 0) {
    setTimeout(() => {
      const idx = toasts.findIndex((t) => t.id === id)
      if (idx > -1) toasts.splice(idx, 1)
    }, duration)
  }
}

function removeToast(id) {
  const idx = toasts.findIndex((t) => t.id === id)
  if (idx > -1) toasts.splice(idx, 1)
}

const toast = (message, type = 'info', duration) => addToast(message, type, duration)
toast.success = (message, duration) => addToast(message, 'success', duration)
toast.error = (message, duration) => addToast(message, 'error', duration)
toast.info = (message, duration) => addToast(message, 'info', duration)

export function useToast() {
  return { toast, toasts: readonly(toasts), removeToast }
}
