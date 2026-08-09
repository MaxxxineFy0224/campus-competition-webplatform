import { ref, readonly } from 'vue'
import { getCompetitions, getTeamPosts } from '../utils/storage'

/**
 * SSE 流式聊天 composable
 *
 * 使用方法：
 *   const { messages, isLoading, sendMessage, clearMessages } = useChat()
 *   await sendMessage('你好，我想参加数学建模竞赛')
 *
 * 生产环境接入真实 SSE 后端时，修改 sendMessage 中的 fetch 地址即可。
 */
export function useChat() {
  const messages = ref([])
  const isLoading = ref(false)
  let abortController = null

  /** 添加一条用户消息 */
  function addUserMessage(text) {
    messages.value.push({ role: 'user', content: text })
  }

  /** 添加一条 AI 消息（逐步追加 content） */
  function addAiMessage(initialContent = '') {
    const msg = { role: 'assistant', content: initialContent }
    messages.value.push(msg)
    return msg
  }

  /** 更新最后一条 AI 消息的 content */
  function updateLastAiContent(chunk) {
    const last = messages.value[messages.value.length - 1]
    if (last && last.role === 'assistant') {
      last.content += chunk
    }
  }

  /** 发送消息（SSE 流式） */
  async function sendMessage(text) {
    if (!text.trim() || isLoading.value) return

    addUserMessage(text.trim())
    isLoading.value = true

    try {
      // 尝试真实 SSE 后端
      await doStreamingFetch(text.trim())
    } catch {
      // 后端不可用时使用本地 mock
      await doMockStreaming(text.trim())
    }

    isLoading.value = false
  }

  /** 真实 SSE 请求 —— 连接后端 /api/chat，自动携带 JWT */
  async function doStreamingFetch(text) {
    abortController = new AbortController()

    const headers = { 'Content-Type': 'application/json' }
    const token = localStorage.getItem('auth_token')
    if (token) headers['Authorization'] = `Bearer ${token}`

    const response = await fetch('/api/ai/stream/chat', {
      method: 'POST',
      headers,
      body: JSON.stringify({ message: text }),
      signal: abortController.signal,
    })

    if (!response.ok) throw new Error('SSE request failed')

    addAiMessage()
    const reader = response.body.getReader()
    const decoder = new TextDecoder()
    let currentEvent = 'message'

    while (true) {
      const { done, value } = await reader.read()
      if (done) break
      const chunk = decoder.decode(value, { stream: true })
      // SSE 格式解析
      const lines = chunk.split('\n')
      for (const line of lines) {
        // 空行 → 事件结束
        if (line === '') {
          currentEvent = 'message'
          continue
        }
        // 容错：event: / data: 后面可能有空格也可能没有（Spring SseEmitter 不带空格）
        if (line.startsWith('event:')) {
          currentEvent = line.slice(6).trim()
        } else if (line.startsWith('data:')) {
          const dataStr = line.slice(5).trim()
          if (currentEvent === 'error') {
            try {
              const parsed = JSON.parse(dataStr)
              updateLastAiContent('⚠️ ' + (parsed.message || 'AI 服务异常'))
            } catch { updateLastAiContent('⚠️ 服务异常，请稍后重试') }
            return
          }
          if (currentEvent === 'done') return // 流结束
          // 普通 content 追加
          try {
            const parsed = JSON.parse(dataStr)
            if (parsed.content) updateLastAiContent(parsed.content)
          } catch { /* skip non-JSON */ }
        }
      }
    }
  }

  /** 本地 Mock AI —— 基于竞赛数据生成智能回复 */
  async function doMockStreaming(text) {
    const competitions = getCompetitions()
    const teamPosts = getTeamPosts()
    const matchedComps = analyzeText(text, competitions)
    const reply = buildReply(text, matchedComps, teamPosts)

    addAiMessage()

    // 模拟流式输出（逐字追加）
    for (let i = 0; i < reply.length; i++) {
      // 检查是否被取消
      if (abortController?.signal.aborted) break
      updateLastAiContent(reply[i])
      await sleep(25 + Math.random() * 15)
    }
  }

  /** 取消当前请求 */
  function cancel() {
    abortController?.abort()
    isLoading.value = false
  }

  /** 清空对话 */
  function clearMessages() {
    messages.value = []
    cancel()
  }

  return {
    messages: readonly(messages),
    isLoading: readonly(isLoading),
    sendMessage,
    cancel,
    clearMessages,
  }
}

// ====================== Mock AI 逻辑 ======================

/** 兴趣关键词映射到竞赛类别 */
const KEYWORD_CATEGORY_MAP = {
  '数学': '数学建模',
  '建模': '数学建模',
  '数模': '数学建模',
  '算法': '编程算法',
  '编程': '编程算法',
  '代码': '编程算法',
  '程序': '编程算法',
  '开发': '编程算法',
  '创业': '创新创业',
  '创新': '创新创业',
  '商业': '创新创业',
  '电子': '电子设计',
  '电路': '电子设计',
  '硬件': '电子设计',
  '嵌入式': '电子设计',
  '机器人': '机器人',
  '机械': '机器人',
  '视觉': '机器人',
  '无人机': '机器人',
  '安全': '信息安全',
  '网络': '信息安全',
  'CTF': '信息安全',
  '黑客': '信息安全',
  '设计': '设计传媒',
  '广告': '设计传媒',
  '传媒': '设计传媒',
  'UI': '设计传媒',
  '英语': '语言文学',
  '外语': '语言文学',
  '写作': '语言文学',
  '智能车': '智能硬件',
  '物联网': '智能硬件',
  '传感器': '智能硬件',
  '计算机': '计算机设计',
  '软件': '计算机设计',
  '前端': '计算机设计',
  '后端': '计算机设计',
  'Python': '编程算法',
  'Java': '编程算法',
  'C++': '编程算法',
  'JavaScript': '编程算法',
  'React': '计算机设计',
  'Vue': '计算机设计',
  '深度学习': '机器人',
  'AI': '机器人',
  '人工智能': '机器人',
  '大数据': '计算机设计',
  '数据分析': '数学建模',
  'MATLAB': '数学建模',
}

/** 从用户文本中提取关键词，匹配竞赛 */
function analyzeText(text, competitions) {
  const matched = new Set()
  const textLower = text.toLowerCase()

  for (const [keyword, category] of Object.entries(KEYWORD_CATEGORY_MAP)) {
    if (textLower.includes(keyword.toLowerCase())) {
      matched.add(category)
    }
  }

  if (matched.size === 0) return competitions.slice(0, 6)

  return competitions
    .filter((c) => matched.has(c.category))
    .sort((a, b) => {
      // 优先匹配状态为"报名中"的
      if (a.status === '报名中' && b.status !== '报名中') return -1
      if (a.status !== '报名中' && b.status === '报名中') return 1
      return b.favoriteCount - a.favoriteCount
    })
}

/** 构建 AI 回复文本 */
function buildReply(text, matchedComps, teamPosts) {
  const lines = []
  const userName = text.match(/我叫(\S+)/)?.[1] || ''
  const greeting = userName ? `你好，${userName}！` : '你好！'

  if (matchedComps.length === 0) {
    lines.push(`${greeting} 很高兴为你服务！`)
    lines.push('')
    lines.push('目前我还没有找到与你需求完全匹配的竞赛项目。你可以告诉我更多关于你的信息，比如：')
    lines.push('• 你擅长什么技术或方向？（如编程、数学、电子、设计等）')
    lines.push('• 你是什么专业、年级？')
    lines.push('• 你想参加什么类型的竞赛？')
    lines.push('')
    lines.push('我会根据你的描述为你精准推荐合适的竞赛和队伍！')
    return lines.join('\n')
  }

  // 构建个性化推荐
  const topCount = Math.min(matchedComps.length, 4)
  const topComps = matchedComps.slice(0, topCount)

  lines.push(`${greeting} 根据你的描述，我为你推荐以下竞赛：`)
  lines.push('')

  topComps.forEach((comp, i) => {
    const statusEmoji = comp.status === '报名中' ? '✅' : comp.status === '即将截止' ? '⚠️' : '📌'
    const teamCount = teamPosts.filter((p) => p.competitionId === comp.id).length
    lines.push(`${i + 1}. ${statusEmoji} **${comp.title}**`)
    lines.push(`   📂 ${comp.category} · ${comp.level} · ${comp.teamSize}`)
    lines.push(`   📅 报名截止：${comp.deadline} · 状态：${comp.status}`)
    if (teamCount > 0) lines.push(`   👥 ${teamCount} 个队伍正在招募队友`)
    lines.push('')
  })

  // 附加建议
  const openComps = matchedComps.filter((c) => c.status === '报名中')
  if (openComps.length > 0) {
    lines.push('💡 **建议**：')
    lines.push(`以上竞赛均在报名期内，建议尽早准备。`)
    lines.push('你可以点击结果卡片查看竞赛详情，浏览正在招募的队伍。')
    lines.push('')
  }

  // 个性化建议
  if (text.includes('大一') || text.includes('大二')) {
    lines.push('🎓 作为低年级同学，建议从校级或省级竞赛开始积累经验，为高年级冲击国奖打好基础！')
  } else if (text.includes('大三') || text.includes('大四')) {
    lines.push('🎓 高年级同学时间宝贵，建议集中精力冲击高含金量的国家级竞赛，对保研和求职都很有帮助！')
  }

  lines.push('')
  lines.push('如果这些推荐不太符合你的期望，请告诉我更多信息，我会重新为你匹配！')

  return lines.join('\n')
}

function sleep(ms) {
  return new Promise((resolve) => setTimeout(resolve, ms))
}