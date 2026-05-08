<script setup lang="ts">
import { onLaunch } from '@dcloudio/uni-app'

onLaunch(() => {
  // 保留应用入口生命周期，避免 App.vue 变成空 SFC。
})
</script>

<style lang="scss">
page {
  min-height: 100%;
  color: #152234;
  background: linear-gradient(180deg, #edf3f8 0%, #ffffff 100%);
  font-family: "PingFang SC", -apple-system, BlinkMacSystemFont, "Hiragino Sans GB", "Microsoft YaHei", sans-serif;
}

view,
text,
button,
image,
scroll-view {
  box-sizing: border-box;
}

view,
text,
button,
scroll-view,
input,
textarea {
  font-family: inherit;
}

button {
  padding: 0;
  margin: 0;
  line-height: normal;
  background: transparent;
  border: 0;
}

button::after {
  border: 0;
}
</style>
