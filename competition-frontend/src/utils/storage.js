const KEY_COMPETITIONS = 'competitions'
const KEY_TEAM_POSTS = 'team_posts'
const KEY_USER = 'user'
const KEY_FAVORITES = 'favorites'

function get(key, fallback) {
  try {
    const v = localStorage.getItem(key)
    return v ? JSON.parse(v) : fallback
  } catch {
    return fallback
  }
}

function set(key, value) {
  localStorage.setItem(key, JSON.stringify(value))
}

export function getCompetitions() {
  return get(KEY_COMPETITIONS, [])
}

export function getCompetitionById(id) {
  return getCompetitions().find((c) => c.id === id)
}

export function saveCompetitions(list) {
  set(KEY_COMPETITIONS, list)
}

export function getTeamPosts() {
  return get(KEY_TEAM_POSTS, [])
}

export function getTeamPostById(id) {
  return getTeamPosts().find((p) => p.id === id)
}

export function saveTeamPosts(list) {
  set(KEY_TEAM_POSTS, list)
}

export function getUser() {
  return get(KEY_USER, null)
}

export function saveUser(user) {
  set(KEY_USER, user)
}

export function getFavorites() {
  return get(KEY_FAVORITES, [])
}

export function toggleFavorite(id, type) {
  const favs = getFavorites()
  const idx = favs.findIndex((f) => f.id === id && f.type === type)
  if (idx > -1) {
    favs.splice(idx, 1)
  } else {
    favs.push({ id, type })
  }
  set(KEY_FAVORITES, favs)
  return favs
}
