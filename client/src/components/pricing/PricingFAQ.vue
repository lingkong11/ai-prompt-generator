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

:deep(.el-collapse-item__header) {
  font-size: 16px;
  font-weight: 600;
  padding: 20px 0;
  border-bottom: 1px solid #e5e7eb;
}

:deep(.el-collapse-item__content) {
  padding: 16px 0;
  font-size: 15px;
  color: #6b7280;
  line-height: 1.6;
}
</style>
