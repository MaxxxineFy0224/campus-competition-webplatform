<template>
  <div class="empty-state">
    <div class="empty-icon">{{ icon }}</div>
    <p class="empty-text">{{ text }}</p>
    <RouterLink v-if="action" :to="action.to" class="btn btn-primary">
      {{ action.label }}
    </RouterLink>
  </div>
</template>

<script setup>
defineProps({
  icon: String,
  text: String,
  action: Object,
})
</script>

<style scoped>
.empty-state {
  text-align: center;
  padding: 48px 24px;
}

.empty-icon {
  font-size: 40px;
  margin-bottom: 12px;
  opacity: 0.4;
}

.empty-text {
  color: #999;
  font-size: 14px;
  margin-bottom: 16px;
  line-height: 1.5;
}
</style>