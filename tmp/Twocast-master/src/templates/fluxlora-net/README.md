## 导入说明
tailwind.config.js
```
const themeConfig = require('./templates/fluxlora-net/tailwind.config')
...existing code

module.exports = deepMerge(baseConfig, themeConfig);
```