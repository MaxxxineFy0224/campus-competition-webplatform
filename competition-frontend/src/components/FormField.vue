<template>
  <div class="form-field">
    <div v-if="label" class="field-label-row">
      <label class="field-label">{{ label }}</label>
      <span v-if="required" class="field-required">*</span>
    </div>
    <slot />
    <div v-if="error" class="field-error">{{ error }}</div>
  </div>
</template>

<script setup>
defineProps({
  label: String,
  required: { type: Boolean, default: false },
  error: String,
})
</script>

<style scoped>
.form-field {
  margin-bottom: 20px;
}

.field-label-row {
  margin-bottom: 8px;
  display: flex;
  align-items: center;
  gap: 4px;
}

.field-label {
  font-size: 14px;
  font-weight: 600;
  color: #333;
}

.field-required {
  color: #ff4d4f;
  font-size: 14px;
  line-height: 1;
}

.field-error {
  font-size: 12px;
  color: #ff4d4f;
  margin-top: 6px;
  display: flex;
  align-items: center;
  gap: 4px;
}

.field-error::before {
  content: '⚠';
  font-size: 11px;
}
</style>