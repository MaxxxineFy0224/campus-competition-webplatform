<template>
  <div class="pref-section">
    <div class="pref-label">{{ label }}</div>
    <div class="pref-options">
      <button
        v-for="opt in options"
        :key="opt"
        type="button"
        class="pref-btn"
        :class="{ active: modelValue === opt }"
        @click="$emit('update:modelValue', modelValue === opt ? '' : opt)"
      >{{ opt }}</button>
    </div>
  </div>
</template>

<script setup>
defineProps({
  label: String,
  options: Array,
  modelValue: { type: String, default: '' },
})

defineEmits(['update:modelValue'])
</script>

<style scoped>
.pref-section {
  margin-bottom: 20px;
}

.pref-label {
  font-size: 14px;
  font-weight: 600;
  margin-bottom: 8px;
  color: #555;
}

.pref-options {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.pref-btn {
  padding: 6px 16px;
  border-radius: 8px;
  border: 1px solid #e5e4e7;
  background: #fff;
  color: #666;
  font-size: 13px;
  cursor: pointer;
  transition: all 0.2s ease;
  font-family: inherit;
}

.pref-btn:hover {
  border-color: #d3adf7;
  background: #f9f0ff;
  color: #722ed1;
}

.pref-btn.active {
  border: 2px solid #722ed1;
  background: #f9f0ff;
  color: #722ed1;
  font-weight: 600;
}
</style>