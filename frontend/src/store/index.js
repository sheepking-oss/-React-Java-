import { create } from 'zustand'
import { persist } from 'zustand/middleware'

const useStore = create(
  persist(
    (set, get) => ({
      user: null,
      token: null,
      unreadCount: 0,

      setUser: (user) => set({ user }),
      setToken: (token) => set({ token }),
      setUnreadCount: (count) => set({ unreadCount: count }),

      login: (user, token) => set({ user, token }),

      logout: () => set({ user: null, token: null, unreadCount: 0 }),

      isLoggedIn: () => !!get().token,
    }),
    {
      name: 'user-storage',
    }
  )
)

export default useStore
