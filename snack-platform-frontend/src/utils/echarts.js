/**
 * ECharts 按需加载配置
 *
 * What: 只引入用到的图表类型和组件，减少打包体积
 * Why: 全量引入 echarts 约 1MB，按需引入可减少约 600KB
 * Why good: 首屏加载更快，移动端体验更好
 */
import * as echarts from 'echarts/core'

// 图表类型
import { BarChart, LineChart, PieChart } from 'echarts/charts'

// 组件
import {
  TitleComponent,
  TooltipComponent,
  LegendComponent,
  GridComponent,
  DatasetComponent,
  TransformComponent
} from 'echarts/components'

// 渲染器
import { CanvasRenderer } from 'echarts/renderers'

// 注册
echarts.use([
  BarChart,
  LineChart,
  PieChart,
  TitleComponent,
  TooltipComponent,
  LegendComponent,
  GridComponent,
  DatasetComponent,
  TransformComponent,
  CanvasRenderer
])

export default echarts
