<template>
  <div class="info-cell">
    <span class="info-icon">{{ icon }}</span>
    <div class="info-content">
      <div class="info-label">{{ label }}</div>
      <div class="info-value">{{ value }}</div>
    </div>
  </div>
</template>

<script setup>
defineProps({
  icon: String,
  label: String,
  value: String,
})
</script>

<style scoped>
.info-cell {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 0;
}

.info-icon {
  font-size: 18px;
  flex-shrink: 0;
  width: 28px;
  text-align: center;
}

.info-content {
  min-width: 0;
}

.info-label {
  font-size: 12px;
  color: #aaa;
  margin-bottom: 2px;
}

.info-value {
  font-size: 14px;
  font-weight: 500;
  color: #333;
}
</style>