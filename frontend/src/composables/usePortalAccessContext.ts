import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { usePortalVisitorStore } from '@/stores/portalVisitor'

export function usePortalAccessContext() {
  const route = useRoute()
  const portalVisitorStore = usePortalVisitorStore()

  const resolvedContext = computed(() =>
    portalVisitorStore.resolveAccessContext(route.query as Record<string, unknown>),
  )

  const matterId = computed(() => resolvedContext.value.matterId)
  const token = computed(() => resolvedContext.value.token)
  const source = computed(() => resolvedContext.value.source)
  const hasStoredContext = computed(() => portalVisitorStore.hasAccessContext)
  const hasAccessContext = computed(() => Boolean(token.value))

  const sourceText = computed(() => {
    if (source.value === 'route') {
      return '当前内容基于地址中的访问令牌加载'
    }
    if (source.value === 'stored') {
      return '当前内容基于最近一次成功访问的事项凭据加载'
    }
    return '当前没有可用的访问上下文'
  })

  function clearAccessContext() {
    portalVisitorStore.clearAccessContext()
  }

  return {
    matterId,
    token,
    source,
    sourceText,
    hasStoredContext,
    hasAccessContext,
    clearAccessContext,
  }
}
