const colors = require('tailwindcss/colors')

module.exports = {
  theme: {
    extend: {
      colors: {
        primary: {
          DEFAULT: 'rgb(106, 102, 196)',
          foreground: colors.white,
          ...colors.indigo,
        },
      },
    },
  },
}
