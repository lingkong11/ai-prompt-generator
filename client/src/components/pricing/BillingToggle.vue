<template>
  <div class="billing-toggle">
    <span :class="{ active: modelValue === 'MONTHLY' }" @click="$emit('update:modelValue', 'MONTHLY')">
      {{ t.pricing.monthly }}
    </span>
    <el-switch
      :model-value="modelValue === 'YEARLY'"
      @update:model-value="$emit('update:modelValue', $event ? 'YEARLY' : 'MONTHLY')"
      active-color="#667eea"
    />
    <span :class="{ active: modelValue === 'YEARLY' }" @click="$emit('update:modelValue', 'YEARLY')">
      {{ t.pricing.yearly }}
    </span>
    <span class="save-badge">{{ t.pricing.save20 }}</span>
  </div>
</template>

<script setup lang="ts">
import type { BillingCycle } from '@/stores/subscription'

defineProps<{
  modelValue: BillingCycle
  t: any
}>()

defineEmits<{
  'update:modelValue': [value: BillingCycle]
}>()
</script>

<style scoped>
.billing-toggle {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 16px;
  margin-bottom: 48px;
}

.billing-toggle span {
  font-size: 16px;
  color: #6b7280;
  cursor: pointer;
  transition: color 0.2s;
}

.billing-toggle span.active {
  color: #111827;
  font-weight: 600;
}

.save-badge {
  background: linear-gradient(135deg, #10b981 0%, #059669 100%);
  color: white;
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 600;
}
</style>
