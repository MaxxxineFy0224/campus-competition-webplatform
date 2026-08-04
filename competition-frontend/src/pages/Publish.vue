<template>
  <div class="publish-page">
    <!-- 背景装饰 -->
    <div class="publish-bg">
      <div class="publish-bg-circle publish-bg-circle-1"></div>
      <div class="publish-bg-circle publish-bg-circle-2"></div>
      <div class="publish-bg-circle publish-bg-circle-3"></div>
    </div>
    <div class="publish-container">
      <div class="page-header">
        <div class="header-icon">
          <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <path d="M12 5v14" />
            <path d="M5 12h14" />
          </svg>
        </div>
        <h1 class="page-title">发布组队需求</h1>
        <p class="page-subtitle">填写信息，快速找到志同道合的队友</p>
      </div>

      <form @submit.prevent="handleSubmit" class="form-card">
        <FormField label="竞赛名称" :required="true" :error="errors.competitionName">
          <input
            type="text"
            v-model="form.competitionName"
            placeholder="例如：全国大学生数学建模竞赛"
            maxlength="20"
            class="form-input"
            :class="{ 'input-error': errors.competitionName }"
          />
        </FormField>

        <FormField label="我的身份" :required="true">
          <div class="role-group">
            <label
              v-for="r in ['队长', '队员']"
              :key="r"
              class="role-capsule"
              :class="{ 'role-active': form.role === r }"
              @click="form.role = r"
            >
              <span class="role-emoji">{{ r === '队长' ? '👑' : '🙋' }}</span>
              {{ r }}
            </label>
          </div>
        </FormField>

        <FormField label="所需技能" :required="true" :error="errors.skills">
          <div class="skill-presets">
            <button
              v-for="skill in SKILL_PRESETS"
              :key="skill"
              type="button"
              class="skill-capsule"
              :class="{ 'skill-active': form.skills.includes(skill) }"
              @click="toggleSkill(skill)"
            >
              {{ skill }}
            </button>
          </div>

          <div v-if="form.skills.length > 0" class="skill-tags">
            <span v-for="s in form.skills" :key="s" class="skill-tag">
              {{ s }}
              <button type="button" class="tag-remove" @click="removeSkill(s)">×</button>
            </span>
          </div>

          <div class="custom-skill-row">
            <input
              ref="customInputRef"
              type="text"
              v-model="customSkill"
              placeholder="输入自定义技能，按回车添加"
              maxlength="15"
              class="form-input"
              @keydown.enter.prevent="addCustomSkill"
            />
            <button type="button" class="btn-add-skill" @click="addCustomSkill">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
                <path d="M12 5v14M5 12h14" />
              </svg>
              添加
            </button>
          </div>
        </FormField>

        <FormField label="联系方式" :required="true" :error="errors.contact">
          <input
            type="text"
            v-model="form.contact"
            placeholder="微信 / QQ / 邮箱"
            maxlength="30"
            class="form-input"
            :class="{ 'input-error': errors.contact }"
          />
        </FormField>

        <FormField label="组队截止日期" :required="true" :error="errors.teamDeadline">
          <input
            type="date"
            v-model="form.teamDeadline"
            :min="getTomorrow()"
            class="form-input"
            :class="{ 'input-error': errors.teamDeadline }"
          />
        </FormField>

        <FormField label="队伍介绍" :required="true" :error="errors.description">
          <div class="textarea-wrapper">
            <textarea
              v-model="form.description"
              placeholder="介绍一下你和你的队伍规划，找队友更高效…"
              rows="5"
              maxlength="500"
              class="form-textarea"
              :class="{ 'input-error': errors.description }"
            />
            <span class="char-counter">
              <span :class="{ 'char-warn': form.description.length >= 480 }">{{ form.description.length }}</span>/500
            </span>
          </div>
        </FormField>

        <button
          type="submit"
          :disabled="submitting"
          class="btn-submit"
          :class="{ 'btn-submitting': submitting }"
        >
          <span v-if="submitting" class="spinner-sm"></span>
          {{ submitting ? '发布中...' : '发布组队信息' }}
          <svg v-if="!submitting" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
            <path d="M22 2 11 13" />
            <path d="M22 2 15 22 11 13 2 9 22 2z" />
          </svg>
        </button>
      </form>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { getUser, getTeamPosts, saveTeamPosts } from '../utils/storage'
import { useToast } from '../composables/useToast'
import FormField from '../components/FormField.vue'

const SKILL_PRESETS = ['前端开发', '后端开发', 'UI设计', '文案策划', '数据分析', '其他']

function getTomorrow() {
  const d = new Date()
  d.setDate(d.getDate() + 1)
  return d.toISOString().split('T')[0]
}

const router = useRouter()
const { toast } = useToast()
const user = getUser()
const customInputRef = ref(null)

const form = reactive({
  competitionName: '',
  role: '队长',
  skills: [],
  contact: '',
  teamDeadline: '',
  description: '',
})

const errors = reactive({})
const submitting = ref(false)
const customSkill = ref('')

function toggleSkill(skill) {
  const idx = form.skills.indexOf(skill)
  if (idx >= 0) form.skills.splice(idx, 1)
  else form.skills.push(skill)
  if (errors.skills) delete errors.skills
}

function addCustomSkill() {
  const val = customSkill.value.trim()
  if (!val) return
  if (val.length > 15) { toast.error('技能名称不能超过15个字'); return }
  if (form.skills.includes(val)) { toast.info('该技能已添加'); customSkill.value = ''; return }
  form.skills.push(val)
  customSkill.value = ''
  if (customInputRef.value) customInputRef.value.focus()
}

function removeSkill(skill) {
  const idx = form.skills.indexOf(skill)
  if (idx > -1) form.skills.splice(idx, 1)
}

function validate() {
  const errs = {}
  if (!form.competitionName.trim()) errs.competitionName = '请输入竞赛名称'
  else if (form.competitionName.trim().length > 20) errs.competitionName = '竞赛名称不能超过20个字'
  if (form.skills.length === 0) errs.skills = '请至少选择一个技能'
  if (!form.contact.trim()) errs.contact = '请输入联系方式'
  else if (form.contact.trim().length < 2 || form.contact.trim().length > 30) errs.contact = '联系方式长度需在2-30字之间'
  if (!form.teamDeadline) errs.teamDeadline = '请选择组队截止日期'
  else {
    const today = new Date(); today.setHours(23, 59, 59, 999)
    if (new Date(form.teamDeadline) <= today) errs.teamDeadline = '请选择未来的日期'
  }
  if (!form.description.trim()) errs.description = '请输入队伍介绍'
  else if (form.description.length > 500) errs.description = '队伍介绍不能超过500字'
  Object.keys(errors).forEach((k) => delete errors[k])
  Object.assign(errors, errs)
  return Object.keys(errors).length === 0
}

function handleSubmit() {
  if (submitting.value) return
  if (!validate()) { toast.error('请检查表单中的错误'); return }
  submitting.value = true
  try {
    const posts = getTeamPosts()
    const newPost = {
      id: 'team-' + Date.now(),
      competitionId: null,
      competitionTitle: form.competitionName.trim(),
      title: `「${form.competitionName.trim()}」组队`,
      author: user?.name || '匿名用户',
      role: form.role,
      needCount: 1, currentCount: 1,
      skills: [...form.skills],
      description: form.description.trim(),
      status: '招募中',
      createdAt: new Date().toISOString(),
      contact: form.contact.trim(),
      teamDeadline: form.teamDeadline,
    }
    saveTeamPosts([newPost, ...posts])
    toast.success('发布成功！')
    setTimeout(() => router.push('/team'), 2000)
  } catch (err) { toast.error('发布失败，请稍后重试'); submitting.value = false }
}
</script>

<style scoped>
.publish-page {
  padding-top: 24px;
  padding-bottom: 80px;
  min-height: 100vh;
  background: linear-gradient(160deg, #f5f7fa 0%, #e8ecf1 100%);
  position: relative;
  overflow: hidden;
}

.publish-container {
  position: relative;
  z-index: 1;
}

/* ===== 背景装饰 ===== */
.publish-bg {
  position: absolute;
  inset: 0;
  pointer-events: none;
  overflow: hidden;
}

.publish-bg-circle {
  position: absolute;
  border-radius: 50%;
}

.publish-bg-circle-1 {
  width: 500px;
  height: 500px;
  background: radial-gradient(circle, rgba(114, 46, 209, 0.04) 0%, transparent 70%);
  top: -150px;
  right: -100px;
  animation: publishFloat 10s ease-in-out infinite;
}

.publish-bg-circle-2 {
  width: 350px;
  height: 350px;
  background: radial-gradient(circle, rgba(22, 119, 255, 0.03) 0%, transparent 70%);
  bottom: 5%;
  left: -80px;
  animation: publishFloat 8s ease-in-out infinite reverse;
}

.publish-bg-circle-3 {
  width: 200px;
  height: 200px;
  background: radial-gradient(circle, rgba(114, 46, 209, 0.03) 0%, transparent 70%);
  top: 50%;
  left: 60%;
  animation: publishFloat 12s ease-in-out infinite 3s;
}

@keyframes publishFloat {
  0%, 100% { transform: translate(0, 0); }
  50% { transform: translate(15px, -15px); }
}

.publish-container {
  width: 100%;
  max-width: 100%;
  margin: 0 auto;
  padding: 0 clamp(16px, 3vw, 48px);
}

.page-header {
  text-align: center;
  margin-bottom: 28px;
}

.header-icon {
  width: 56px;
  height: 56px;
  margin: 0 auto 16px;
  border-radius: 16px;
  background: linear-gradient(135deg, #722ed1, #9254de);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  box-shadow: 0 4px 16px rgba(114, 46, 209, 0.3);
}

.page-title {
  font-size: 28px;
  font-weight: 700;
  margin: 0 0 8px;
  color: #1a1a2e;
}

.page-subtitle {
  color: #888;
  font-size: 14px;
  margin: 0;
}

.form-card {
  background: #fff;
  border-radius: 16px;
  padding: 32px 28px;
  box-shadow: 0 4px 24px rgba(0, 0, 0, 0.06), 0 1px 4px rgba(0, 0, 0, 0.04);
  border: 1px solid rgba(255, 255, 255, 0.8);
  transition: box-shadow 0.3s ease;
  max-width: 800px;
  margin: 0 auto;
}

.form-card:hover {
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.08);
}

.form-input {
  width: 100%;
  padding: 12px 16px;
  border: 1.5px solid #e5e4e7;
  border-radius: 10px;
  font-size: 14px;
  font-family: inherit;
  outline: none;
  transition: all 0.25s ease;
  box-sizing: border-box;
  background: #fafafa;
}

.form-input:focus {
  border-color: #722ed1;
  box-shadow: 0 0 0 3px rgba(114, 46, 209, 0.12);
  background: #fff;
}

.form-input.input-error {
  border-color: #ff4d4f;
}

.form-input.input-error:focus {
  box-shadow: 0 0 0 3px rgba(255, 77, 79, 0.12);
}

.form-textarea {
  width: 100%;
  padding: 12px 16px;
  padding-bottom: 32px;
  border: 1.5px solid #e5e4e7;
  border-radius: 10px;
  font-size: 14px;
  font-family: inherit;
  outline: none;
  resize: vertical;
  transition: all 0.25s ease;
  box-sizing: border-box;
  background: #fafafa;
  min-height: 100px;
  line-height: 1.6;
}

.form-textarea:focus {
  border-color: #722ed1;
  box-shadow: 0 0 0 3px rgba(114, 46, 209, 0.12);
  background: #fff;
}

.form-textarea.input-error {
  border-color: #ff4d4f;
}

.form-textarea.input-error:focus {
  box-shadow: 0 0 0 3px rgba(255, 77, 79, 0.12);
}

.textarea-wrapper {
  position: relative;
}

.char-counter {
  position: absolute;
  right: 12px;
  bottom: 10px;
  font-size: 12px;
  color: #ccc;
  pointer-events: none;
  user-select: none;
}

.char-warn {
  color: #ff4d4f;
}

.role-group {
  display: flex;
  gap: 12px;
}

.role-capsule {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 12px 16px;
  border-radius: 10px;
  border: 2px solid #e5e4e7;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.25s ease;
  background: #fafafa;
  color: #666;
  user-select: none;
}

.role-capsule:hover {
  border-color: #d3adf7;
  background: #f9f0ff;
  color: #722ed1;
}

.role-capsule.role-active {
  border-color: #722ed1;
  background: #f9f0ff;
  color: #722ed1;
  font-weight: 600;
  box-shadow: 0 0 0 3px rgba(114, 46, 209, 0.12);
}

.role-emoji {
  font-size: 16px;
}

.skill-presets {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
  margin-bottom: 12px;
}

.skill-capsule {
  padding: 7px 18px;
  border-radius: 20px;
  border: 1.5px solid #e5e4e7;
  background: #fafafa;
  color: #666;
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.25s ease;
  user-select: none;
}

.skill-capsule:hover {
  border-color: #d3adf7;
  background: #f9f0ff;
  color: #722ed1;
}

.skill-capsule.skill-active {
  border-color: #722ed1;
  background: #f9f0ff;
  color: #722ed1;
  font-weight: 600;
  box-shadow: 0 0 0 3px rgba(114, 46, 209, 0.12);
}

.skill-tags {
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
  margin-bottom: 12px;
}

.skill-tag {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 5px 12px;
  background: #f9f0ff;
  color: #722ed1;
  border-radius: 8px;
  font-size: 13px;
  font-weight: 500;
  border: 1px solid #d3adf7;
}

.tag-remove {
  background: none;
  border: none;
  color: #9254de;
  font-size: 16px;
  cursor: pointer;
  padding: 0 2px;
  line-height: 1;
  transition: color 0.15s ease;
}

.tag-remove:hover {
  color: #ff4d4f;
}

.custom-skill-row {
  display: flex;
  gap: 8px;
}

.custom-skill-row .form-input {
  flex: 1;
}

.btn-add-skill {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  flex-shrink: 0;
  padding: 10px 18px;
  font-size: 13px;
  font-weight: 500;
  border-radius: 10px;
  border: 1.5px solid #722ed1;
  background: #fff;
  color: #722ed1;
  cursor: pointer;
  transition: all 0.25s ease;
  white-space: nowrap;
}

.btn-add-skill:hover {
  background: #f9f0ff;
  box-shadow: 0 0 0 3px rgba(114, 46, 209, 0.12);
}

.btn-submit {
  width: 100%;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 16px 0;
  font-size: 16px;
  font-weight: 600;
  margin-top: 8px;
  border-radius: 12px;
  border: none;
  cursor: pointer;
  transition: all 0.3s ease;
  color: #fff;
  background: linear-gradient(135deg, #722ed1 0%, #531dab 100%);
  box-shadow: 0 4px 14px rgba(114, 46, 209, 0.3);
  letter-spacing: 0.5px;
  font-family: inherit;
}

.btn-submit:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 6px 24px rgba(114, 46, 209, 0.4);
}

.btn-submit:active:not(:disabled) {
  transform: translateY(0);
  box-shadow: 0 2px 8px rgba(114, 46, 209, 0.3);
}

.btn-submit.btn-submitting {
  opacity: 0.6;
  cursor: not-allowed;
}

.spinner-sm {
  width: 18px;
  height: 18px;
  border: 2px solid rgba(255,255,255,0.3);
  border-top-color: #fff;
  border-radius: 50%;
  animation: spin 0.6s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

@media (max-width: 480px) {
  .publish-page {
    padding-top: 16px;
    padding-bottom: 60px;
  }

  .publish-container {
    padding: 0 clamp(12px, 4vw, 16px);
  }

  .form-card {
    padding: clamp(16px, 4vw, 24px);
    border-radius: 12px;
  }

  .page-title {
    font-size: 22px;
  }

  .role-group {
    gap: 8px;
  }

  .role-capsule {
    padding: 10px 12px;
    font-size: 13px;
  }

  .skill-presets {
    gap: 6px;
  }

  .skill-capsule {
    padding: 5px 12px;
    font-size: 12px;
  }

  .custom-skill-row {
    flex-direction: column;
    gap: 8px;
  }

  .btn-add-skill {
    width: 100%;
    justify-content: center;
  }

  .btn-submit {
    padding: 14px 0;
    font-size: 15px;
  }
}
</style>