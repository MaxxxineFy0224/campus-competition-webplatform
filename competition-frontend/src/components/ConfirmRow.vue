<template>
  <div class="confirm-row">
    <span class="confirm-label">{{ label }}</span>
    <span class="confirm-value">{{ value }}</span>
  </div>
</template>

<script setup>
defineProps({
  label: String,
  value: String,
})
</script>

<style scoped>
.confirm-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 14px;
  padding: 6px 0;
}

.confirm-label {
  color: #888;
}

.confirm-value {
  font-weight: 600;
  color: #722ed1;
}
</style>