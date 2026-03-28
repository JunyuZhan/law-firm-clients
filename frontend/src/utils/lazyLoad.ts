import logger from '@/utils/logger'
import { ref, onBeforeUnmount, type Ref } from 'vue'

interface LazyImageOptions {
  root?: Element | null
  rootMargin?: string
  threshold?: number
  dataSrc?: string
  onLoad?: (element: HTMLImageElement) => void
  onError?: (element: HTMLImageElement) => void
}

class LazyLoader {
  private observer: IntersectionObserver | null = null
  private elements: Map<Element, LazyImageOptions> = new Map()

  constructor() {
    this.init()
  }

  private init() {
    if ('IntersectionObserver' in window) {
      this.observer = new IntersectionObserver(
        this.handleIntersect.bind(this),
        {
          rootMargin: '50px 0px',
          threshold: 0.01,
        }
      )
    }
  }

  /**
   * 注册懒加载图片
   * @param selector CSS 选择器
   * @param options 配置选项
   */
  observe(selector: string, options: LazyImageOptions = {}): void {
    if (!this.observer) {
      logger.warn('IntersectionObserver not supported')
      this.fallback(selector)
      return
    }

    const elements = document.querySelectorAll<HTMLImageElement>(selector)
    elements.forEach((el) => {
      const dataSrc = options.dataSrc || el.dataset.src
      if (dataSrc) {
        this.elements.set(el, {
          ...options,
          dataSrc,
        })
        this.observer!.observe(el)
      }
    })
  }

  /**
   * 观察单个元素
   * @param element 要观察的元素
   * @param options 配置选项
   */
  observeElement(element: HTMLImageElement, options: LazyImageOptions = {}): void {
    if (!this.observer) {
      logger.warn('IntersectionObserver not supported')
      return
    }

    const dataSrc = options.dataSrc || element.dataset.src
    if (dataSrc) {
      this.elements.set(element, {
        ...options,
        dataSrc,
      })
      this.observer!.observe(element)
    }
  }

  /**
   * 处理交叉事件
   */
  private handleIntersect(entries: IntersectionObserverEntry[]): void {
    entries.forEach((entry) => {
      if (entry.isIntersecting) {
        const element = entry.target as HTMLImageElement
        const options = this.elements.get(element)

        if (options?.dataSrc) {
          this.loadImage(element, options.dataSrc, options)
          this.observer?.unobserve(element)
          this.elements.delete(element)
        }
      }
    })
  }

  /**
   * 加载图片
   */
  private loadImage(
    element: HTMLImageElement,
    src: string,
    options: LazyImageOptions
  ): void {
    const img = new Image()
    img.src = src

    img.onload = () => {
      element.src = src
      element.classList.add('lazy-loaded')
      element.classList.remove('lazy-loading')
      options.onLoad?.(element)
    }

    img.onerror = () => {
      element.classList.add('lazy-error')
      element.classList.remove('lazy-loading')
      options.onError?.(element)
    }
  }

  /**
   * 不支持 IntersectionObserver 时的回退方案
   */
  private fallback(selector: string): void {
    const elements = document.querySelectorAll<HTMLImageElement>(selector)
    elements.forEach((el) => {
      const dataSrc = el.dataset.src
      if (dataSrc) {
        el.src = dataSrc
      }
    })
  }

  /**
   * 断开观察器
   */
  disconnect(): void {
    this.observer?.disconnect()
    this.elements.clear()
  }
}

// 创建全局懒加载实例
const lazyLoader = new LazyLoader()

/**
 * 延迟加载图片
 * @param selector CSS 选择器，默认为 '.lazy-image'
 * @param options 配置选项
 */
export function lazyLoadImages(
  selector: string = '.lazy-image',
  options: LazyImageOptions = {}
): void {
  lazyLoader.observe(selector, options)
}

/**
 * 延迟加载单个图片
 * @param element 图片元素
 * @param options 配置选项
 */
export function lazyLoadImage(
  element: HTMLImageElement,
  options: LazyImageOptions = {}
): void {
  lazyLoader.observeElement(element, options)
}

/**
 * 断开懒加载观察器
 */
export function disconnectLazyLoader(): void {
  lazyLoader.disconnect()
}

/**
 * 初始化所有懒加载图片
 * 自动查找所有带有 'data-src' 属性的图片
 */
export function initLazyImages(): void {
  lazyLoadImages('img[data-src]', {
    onLoad: (element) => {
      logger.info('图片加载完成:', element.src)
    },
    onError: (element) => {
      logger.error('图片加载失败:', element.dataset.src)
    },
  })
}

export function useLazyImage() {
  const observerRef = ref<IntersectionObserver | null>(null)

  const observe = (
    elementRef: Ref<HTMLImageElement | null>,
    src: string,
    onLoad?: () => void
  ) => {
    if (!elementRef.value || !('IntersectionObserver' in window)) {
      return
    }

    const observer = new IntersectionObserver((entries) => {
      entries.forEach((entry) => {
        if (entry.isIntersecting) {
          const img = entry.target as HTMLImageElement
          img.src = src
          observerRef.value?.unobserve(img)
          onLoad?.()
        }
      })
    })

    observerRef.value = observer
    observer.observe(elementRef.value)
  }

  const disconnect = () => {
    observerRef.value?.disconnect()
  }

  onBeforeUnmount(disconnect)

  return {
    observe,
    disconnect,
  }
}
