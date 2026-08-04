<template>
  <div class="list-item" @click="handleClick">
    <div class="list-item-body">
      <template v-if="comp">
        <div class="list-item-title">{{ comp.title }}</div>
        <div class="list-item-meta">{{ comp.category }} · {{ comp.level }} · {{ comp.teamSize }}</div>
      </template>
      <template v-else-if="post">
        <div class="list-item-title">{{ post.title }}</div>
        <div class="list-item-meta">{{ post.competitionTitle }} · {{ post.author }}</div>
      </template>
    </div>
    <button
      v-if="hasAction"
      class="list-item-action"
      :class="{ 'action-danger': actionDanger }"
      @click.stop="handleAction"
    >
      {{ actionLabel }}
    </button>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'

const props = defineProps({
  post: Object,
  comp: Object,
  to: String,
  actionLabel: { type: String, default: '操作' },
  actionDanger: { type: Boolean, default: false },
})

const emit = defineEmits(['action', 'click'])
const router = useRouter()

const hasAction = computed(() => {
  return !!props.actionLabel && props.actionLabel !== '操作'
})

function handleClick(e) {
  if (props.to) {
    router.push(props.to)
    e.preventDefault()
    e.stopPropagation()
  }
  emit('click', e)
}

function handleAction(e) {
  e.preventDefault()
  e.stopPropagation()
  emit('action', e)
}
</script>

<style scoped>
.list-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px 20px;
  border-bottom: 1px solid #f5f5f5;
  cursor: pointer;
  transition: background 0.15s ease;
  gap: 12px;
}

.list-item:hover {
  background: #fafafa;
}

.list-item:last-child {
  border-bottom: none;
}

.list-item-body {
  flex: 1;
  min-width: 0;
}

.list-item-title {
  font-weight: 600;
  font-size: 15px;
  margin-bottom: 4px;
  line-height: 1.4;
  color: #1f1f1f;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.list-item-meta {
  font-size: 13px;
  color: #888;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.list-item-action {
  background: none;
  border: none;
  cursor: pointer;
  font-size: 13px;
  padding: 6px 12px;
  border-radius: 6px;
  font-weight: 500;
  flex-shrink: 0;
  transition: background 0.15s ease;
  color: #1677ff;
}

.list-item-action:hover {
  background: #f0f5ff;
}

.list-item-action.action-danger {
  color: #ff4d4f;
}

.list-item-action.action-danger:hover {
  background: #fff1f0;
}
</style>