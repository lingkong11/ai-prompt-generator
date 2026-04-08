<template>
  <div class="pricing-faq">
    <h2 class="section-title">{{ t.pricing.faqTitle }}</h2>
    <el-collapse v-model="activeNames">
      <el-collapse-item
        v-for="(item, index) in faqItems"
        :key="index"
        :title="item.question"
        :name="String(index)"
      >
        <p>{{ item.answer }}</p>
      </el-collapse-item>
    </el-collapse>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'

const props = defineProps<{
  t: any
}>()

const activeNames = ref<string[]>([])

const faqItems = computed(() => [
  {
    question: props.t.pricing.faqCancelQuestion,
    answer: props.t.pricing.faqCancelAnswer
  },
  {
    question: props.t.pricing.faqAdsQuestion,
    answer: props.t.pricing.faqAdsAnswer
  },
  {
    question: props.t.pricing.faqRefundQuestion,
    answer: props.t.pricing.faqRefundAnswer
  },
  {
    question: props.t.pricing.faqInvoiceQuestion,
    answer: props.t.pricing.faqInvoiceAnswer
  },
  {
    question: props.t.pricing.faqPaymentQuestion,
    answer: props.t.pricing.faqPaymentAnswer
  }
])
</script>

<style scoped>
.pricing-faq {
  max-width: 700px;
  margin: 80px auto 0;
}

.section-title {
  text-align: center;
  font-size: 32px;
  font-weight: 700;
  margin-bottom: 40px;
  color: var(--text-primary);
}

:deep(.el-collapse) {
  border: none;
}

:deep(.el-collapse-item) {
  margin-bottom: 12px;
}

:deep(.el-collapse-item__header) {
  font-size: 16px;
  font-weight: 600;
  padding: 20px 24px;
  border-radius: 12px;
  background: var(--card-bg);
  border: 1px solid rgba(0, 0, 0, 0.05);
  transition: all 0.3s ease;
}

:deep(.el-collapse-item__header:hover) {
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.1);
}

:deep(.el-collapse-item__wrap) {
  border-bottom-left-radius: 12px;
  border-bottom-right-radius: 12px;
  background: var(--card-bg);
  margin-top: -8px;
  padding-top: 8px;
}

:deep(.el-collapse-item__content) {
  padding: 16px 24px 24px;
  font-size: 15px;
  color: var(--text-secondary);
  line-height: 1.6;
}
</style>
