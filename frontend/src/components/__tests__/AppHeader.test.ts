import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import AppHeader from '../AppHeader.vue'
import { useAppConfigStore } from '@/stores/appConfig'

describe('AppHeader', () => {
  const defaultProps = {
    variant: 'portal' as const,
    showBack: false,
    showBackText: true,
    backText: '返回',
    logoUrl: '/logo.png',
    lawFirmName: '北京某某律师事务所',
  }

  it('renders correctly with portal variant', () => {
    const wrapper = mount(AppHeader, {
      props: defaultProps,
      global: {
        plugins: [createPinia()],
        stubs: {
          'a-layout-header': true,
          'a-button': true,
          ArrowLeftOutlined: true,
        },
      },
    })

    expect(wrapper.find('.app-header').exists()).toBe(true)
    expect(wrapper.classes()).toContain('portal')
  })

  it('shows back button when showBack is true', () => {
    const wrapper = mount(AppHeader, {
      props: {
        ...defaultProps,
        showBack: true,
      },
      global: {
        plugins: [createPinia()],
        stubs: {
          'a-layout-header': {
            template: '<div class="app-header"><slot /></div>',
          },
          'a-button': {
            template: '<button class="header-btn back-btn"><slot /></button>',
          },
          ArrowLeftOutlined: true,
        },
      },
    })

    expect(wrapper.find('.back-btn').exists()).toBe(true)
  })

  it('emits back event when back button is clicked', async () => {
    const wrapper = mount(AppHeader, {
      props: {
        ...defaultProps,
        showBack: true,
      },
      global: {
        plugins: [createPinia()],
        stubs: {
          'a-layout-header': {
            template: '<div class="app-header"><slot /></div>',
          },
          'a-button': {
            template: '<button class="header-btn back-btn" @click="$emit(\'click\')"><slot /></button>',
          },
          ArrowLeftOutlined: true,
        },
      },
    })

    await wrapper.find('.back-btn').trigger('click')
    expect(wrapper.emitted()).toHaveProperty('back')
  })

  it('shows back text when showBackText is true', () => {
    const wrapper = mount(AppHeader, {
      props: {
        ...defaultProps,
        showBack: true,
        showBackText: true,
        backText: '返回首页',
      },
      global: {
        plugins: [createPinia()],
        stubs: {
          'a-layout-header': {
            template: '<div class="app-header"><slot /></div>',
          },
          'a-button': {
            template: '<button class="header-btn back-btn"><slot /></button>',
          },
          ArrowLeftOutlined: true,
        },
      },
    })

    expect(wrapper.find('.back-btn-text').exists()).toBe(true)
    expect(wrapper.find('.back-btn-text').text()).toBe('返回首页')
  })

  it('hides back text when showBackText is false', () => {
    const wrapper = mount(AppHeader, {
      props: {
        ...defaultProps,
        showBack: true,
        showBackText: false,
      },
      global: {
        plugins: [createPinia()],
        stubs: {
          'a-layout-header': {
            template: '<div class="app-header"><slot /></div>',
          },
          'a-button': {
            template: '<button class="header-btn back-btn"><slot /></button>',
          },
          ArrowLeftOutlined: true,
        },
      },
    })

    expect(wrapper.find('.back-btn-text').exists()).toBe(false)
  })

  it('renders logo section for portal variant', () => {
    // 设置 Pinia store 数据
    const pinia = createPinia()
    setActivePinia(pinia)
    const appConfigStore = useAppConfigStore()
    appConfigStore.appName = '北京某某律师事务所'
    appConfigStore.lawFirmName = '北京某某律师事务所'
    appConfigStore.logoUrl = '/logo.png'
    appConfigStore.appShortName = '某某客户服务'
    appConfigStore.appSlogan = '专业事项，一个清晰的客户入口'

    const wrapper = mount(AppHeader, {
      props: defaultProps,
      global: {
        plugins: [pinia],
        stubs: {
          'a-layout-header': {
            template: '<div class="app-header"><slot /></div>',
          },
          'a-button': true,
          ArrowLeftOutlined: true,
        },
      },
    })

    expect(wrapper.find('.logo-section').exists()).toBe(true)
    expect(wrapper.find('.logo-text h1').text().trim()).toBe('北京某某律师事务所')
    expect(wrapper.find('.logo-system-label').text().trim()).toBe('某某客户服务')
    expect(wrapper.find('.logo-slogan').text().trim()).toBe('专业事项，一个清晰的客户入口')
  })

  it('does not render logo section for detail variant', () => {
    const wrapper = mount(AppHeader, {
      props: {
        ...defaultProps,
        variant: 'detail',
      },
      global: {
        plugins: [createPinia()],
        stubs: {
          'a-layout-header': {
            template: '<div class="app-header"><slot /></div>',
          },
          'a-button': true,
          ArrowLeftOutlined: true,
        },
      },
    })

    expect(wrapper.find('.logo-section').exists()).toBe(false)
  })

  it('renders left slot content', () => {
    const wrapper = mount(AppHeader, {
      props: defaultProps,
      slots: {
        left: '<div class="custom-left">Custom Left</div>',
      },
      global: {
        plugins: [createPinia()],
        stubs: {
          'a-layout-header': {
            template: '<div class="app-header"><slot /></div>',
          },
          'a-button': true,
          ArrowLeftOutlined: true,
        },
      },
    })

    expect(wrapper.find('.custom-left').exists()).toBe(true)
    expect(wrapper.find('.custom-left').text()).toBe('Custom Left')
  })

  it('renders center slot content', () => {
    const wrapper = mount(AppHeader, {
      props: defaultProps,
      slots: {
        center: '<div class="custom-center">Custom Center</div>',
      },
      global: {
        plugins: [createPinia()],
        stubs: {
          'a-layout-header': {
            template: '<div class="app-header"><slot /></div>',
          },
          'a-button': true,
          ArrowLeftOutlined: true,
        },
      },
    })

    expect(wrapper.find('.custom-center').exists()).toBe(true)
    expect(wrapper.find('.custom-center').text()).toBe('Custom Center')
  })

  it('renders right slot content', () => {
    const wrapper = mount(AppHeader, {
      props: defaultProps,
      slots: {
        right: '<div class="custom-right">Custom Right</div>',
      },
      global: {
        plugins: [createPinia()],
        stubs: {
          'a-layout-header': {
            template: '<div class="app-header"><slot /></div>',
          },
          'a-button': true,
          ArrowLeftOutlined: true,
        },
      },
    })

    expect(wrapper.find('.custom-right').exists()).toBe(true)
    expect(wrapper.find('.custom-right').text()).toBe('Custom Right')
  })

  it('applies correct CSS classes based on variant', () => {
    const wrapper = mount(AppHeader, {
      props: {
        ...defaultProps,
        variant: 'detail',
      },
      global: {
        stubs: {
          'a-layout-header': true,
          'a-button': true,
          ArrowLeftOutlined: true,
        },
      },
    })

    expect(wrapper.classes()).toContain('detail')
    expect(wrapper.classes()).toContain('has-welcome')
  })
})
