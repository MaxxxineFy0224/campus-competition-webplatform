<template>
  <Teleport to="body">
    <Transition name="modal">
      <div
        v-if="modal.current"
        class="modal-overlay"
        @click.self="closeModal()"
      >
        <div class="modal-box">
          <div class="modal-icon" v-if="modal.current.icon">
            <span>{{ modal.current.icon }}</span>
          </div>
          <h3 class="modal-title">{{ modal.current.title }}</h3>
          <p class="modal-message">{{ modal.current.message }}</p>
          <div class="modal-actions">
            <button
              v-if="modal.current.showCancel"
              class="btn btn-outline modal-btn"
              @click="closeModal()"
            >
              {{ modal.current.cancelText || '取消' }}
            </button>
            <button
              class="btn btn-primary modal-btn"
              @click="confirmOk"
            >
              {{ modal.current.confirmText || '确认' }}
            </button>
          </div>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>

<script setup>
import { useModal } from '../composables/useModal'

const { modal, closeModal } = useModal()

function confirmOk() {
  closeModal(true)
}
</script>

<style scoped>
.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 9998;
  padding: 24px;
  backdrop-filter: blur(4px);
}

.modal-box {
  background: #fff;
  border-radius: 18px;
  padding: 28px;
  min-width: 320px;
  max-width: 420px;
  box-shadow: 0 16px 48px rgba(0, 0, 0, 0.2);
  animation: modalIn 0.25s cubic-bezier(0.4, 0, 0.2, 1);
}

.modal-icon {
  width: 48px;
  height: 48px;
  margin: 0 auto 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
  background: #f0f5ff;
  border-radius: 50%;
}

.modal-title {
  font-size: 18px;
  font-weight: 700;
  color: #1f1f1f;
  margin-bottom: 12px;
  text-align: center;
}

.modal-message {
  color: #555;
  font-size: 15px;
  line-height: 1.6;
  margin-bottom: 24px;
  text-align: center;
  white-space: pre-wrap;
}

.modal-actions {
  display: flex;
  gap: 10px;
  justify-content: flex-end;
}

.modal-btn {
  padding: 10px 24px;
  font-size: 14px;
  border-radius: 10px;
  min-width: 80px;
}

@keyframes modalIn {
  from {
    opacity: 0;
    transform: scale(0.92) translateY(8px);
  }
  to {
    opacity: 1;
    transform: scale(1) translateY(0);
  }
}

/* Transition classes */
.modal-enter-active {
  animation: modalFadeIn 0.25s ease;
}

.modal-leave-active {
  animation: modalFadeOut 0.2s ease;
}

@keyframes modalFadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

@keyframes modalFadeOut {
  from { opacity: 1; }
  to { opacity: 0; }
}
</style>