<template>
  <div class="mall-operations">
    <el-tabs v-model="activeTab">
      <el-tab-pane label="商品管理" name="products">
        <div class="product-actions mb-4">
          <el-button type="primary" @click="handleAddProduct">
            <el-icon class="mr-2"><Plus /></el-icon>添加商品
          </el-button>
        </div>

        <el-table :data="productList" style="width: 100%" v-loading="loading" border>
          <el-table-column prop="id" label="ID" width="60" />
          <el-table-column label="图片" width="100">
             <template #default="scope">
                <el-image 
                  style="width: 50px; height: 50px"
                  :src="scope.row.imageUrl" 
                  :preview-src-list="[scope.row.imageUrl]"
                  fit="cover"
                  v-if="scope.row.imageUrl"
                />
                <span v-else>无图片</span>
             </template>
          </el-table-column>
          <el-table-column prop="name" label="商品名称" width="150" />
          <el-table-column prop="price" label="价格" width="100">
             <template #default="scope">
               <span style="color: #E6A23C; font-weight: bold;">{{ scope.row.price }} 积分</span>
             </template>
          </el-table-column>
          <el-table-column prop="stock" label="库存" width="100" />
          <el-table-column prop="type" label="分类" width="100" />
          <el-table-column prop="description" label="描述" show-overflow-tooltip />
          <el-table-column label="操作" width="180" fixed="right">
            <template #default="scope">
              <el-button size="small" @click="handleEditProduct(scope.row)">编辑</el-button>
              <el-popconfirm title="确定删除该商品吗?" @confirm="handleDeleteProduct(scope.row.id)">
                <template #reference>
                  <el-button size="small" type="danger">删除</el-button>
                </template>
              </el-popconfirm>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <el-tab-pane label="订单审计" name="orders">
        <div class="mb-4">
          <el-input v-model="orderSearch.userId" placeholder="用户ID" style="width: 150px;" class="mr-2" />
          <el-input v-model="orderSearch.orderId" placeholder="订单ID" style="width: 150px;" class="mr-2" />
          <el-button type="primary" @click="loadOrders">查询</el-button>
        </div>

        <el-table :data="orderList" style="width: 100%" v-loading="orderLoading" border>
          <el-table-column prop="id" label="订单ID" width="80" />
          <el-table-column prop="userId" label="用户ID" width="80" />
          <el-table-column prop="productId" label="商品ID" width="80" />
          <el-table-column prop="price" label="金额" width="100" />
          <el-table-column prop="status" label="状态" width="100">
            <template #default="scope">
              <el-tag v-if="scope.row.status === 1" type="info">已退款</el-tag>
              <el-tag v-else-if="scope.row.isAbnormal" type="danger">异常</el-tag>
              <el-tag v-else type="success">成功</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="createTime" label="创建时间" width="180" :formatter="formatDate" />
          <el-table-column label="收货信息" width="100">
             <template #default="scope">
                 <el-popover
                     placement="top"
                     title="收货信息"
                     :width="250"
                     trigger="click"
                     v-if="scope.row.shippingAddress"
                 >
                     <template #reference>
                         <el-button size="small" link>查看</el-button>
                     </template>
                     <p><strong>收货人:</strong> {{ scope.row.receiverName }}</p>
                     <p><strong>电话:</strong> {{ scope.row.receiverPhone }}</p>
                     <p><strong>地址:</strong> {{ scope.row.shippingAddress }}</p>
                     <p v-if="scope.row.trackingNumber"><strong>快递单号:</strong> {{ scope.row.trackingNumber }}</p>
                     <p v-if="scope.row.shippingStatus !== undefined">
                         <strong>状态:</strong> 
                         <el-tag size="small" :type="scope.row.shippingStatus === 1 ? 'success' : 'info'">
                             {{ scope.row.shippingStatus === 1 ? '已发货' : '待发货' }}
                         </el-tag>
                     </p>
                 </el-popover>
                 <span v-else class="text-gray-400">无</span>
             </template>
          </el-table-column>
          <el-table-column label="操作" width="200" fixed="right">
            <template #default="scope">
              <el-button size="small" type="primary" v-if="scope.row.shippingAddress && scope.row.shippingStatus === 0 && scope.row.status === 0" @click="handleShip(scope.row)">发货</el-button>
              <el-button size="small" type="warning" v-if="!scope.row.isAbnormal && scope.row.status === 0" @click="handleMarkAbnormal(scope.row.id)">标记异常</el-button>
              <el-button size="small" type="info" v-if="scope.row.isAbnormal" @click="handleCancelAbnormal(scope.row.id)">取消异常</el-button>
              <el-popconfirm title="确定强制退款?" @confirm="handleRefund(scope.row.id)" v-if="scope.row.status === 0">
                <template #reference>
                  <el-button size="small" type="danger">强制退款</el-button>
                </template>
              </el-popconfirm>
            </template>
          </el-table-column>
        </el-table>
        
        <el-pagination
            class="mt-4"
            background
            layout="prev, pager, next"
            :total="orderTotal"
            :page-size="orderPageSize"
            @current-change="handleOrderPageChange"
        />
      </el-tab-pane>
    </el-tabs>

    <!-- Product Dialog -->
    <el-dialog v-model="dialogVisible" :title="dialogType === 'add' ? '添加商品' : '编辑商品'" width="500px">
      <el-form :model="productForm" label-width="80px">
        <el-form-item label="名称">
          <el-input v-model="productForm.name" />
        </el-form-item>
        <el-form-item label="价格">
          <el-input-number v-model="productForm.price" :min="0" />
        </el-form-item>
        <el-form-item label="库存">
          <el-input-number v-model="productForm.stock" :min="0" />
        </el-form-item>
        <el-form-item label="分类">
          <el-select v-model="productForm.type" placeholder="选择分类">
            <el-option label="头像框" value="AVATAR_FRAME" />
            <el-option label="皮肤" value="SKIN" />
            <el-option label="静态背景" value="STATIC_BG" />
            <el-option label="动态背景" value="DYNAMIC_BG" />
            <el-option label="称号" value="TITLE" />
            <el-option label="实物" value="PHYSICAL" />
            <el-option label="优惠券" value="COUPON" />
          </el-select>
        </el-form-item>
        <el-form-item label="有效天数" v-if="['AVATAR_FRAME', 'SKIN', 'STATIC_BG', 'DYNAMIC_BG', 'TITLE'].includes(productForm.type)">
             <el-input-number v-model="productForm.days" :min="0" placeholder="0为永久" />
             <span class="ml-2 text-gray-400">天 (0为永久)</span>
        </el-form-item>
        <el-form-item label="视频URL" v-if="productForm.type === 'DYNAMIC_BG'">
             <el-input v-model="productForm.videoUrl" placeholder="动态背景视频链接" />
        </el-form-item>
        <el-form-item label="图片">
          <el-upload
            class="avatar-uploader"
            action="/api/admin/shop/upload"
            :show-file-list="false"
            :on-success="handleAvatarSuccess"
            :before-upload="beforeAvatarUpload"
            :headers="uploadHeaders"
          >
            <img v-if="productForm.imageUrl" :src="productForm.imageUrl" class="avatar" />
            <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
          </el-upload>
          <el-input v-model="productForm.imageUrl" placeholder="或输入图片URL" class="mt-2" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="productForm.description" type="textarea" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitProduct">确定</el-button>
        </span>
      </template>
    </el-dialog>
    <!-- Ship Dialog -->
    <el-dialog v-model="shipDialogVisible" title="订单发货" width="400px">
      <el-form :model="shipForm" label-width="80px">
        <el-form-item label="快递单号">
          <el-input v-model="shipForm.trackingNumber" placeholder="请输入快递单号" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="shipDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitShip">确定发货</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { Plus } from '@element-plus/icons-vue'
import { 
    getOrders, 
    refundOrder, 
    markOrderAbnormal, 
    cancelOrderAbnormal,
    shipOrder,
    saveProduct, 
    deleteProduct,
    getAdminProducts
} from '../../api/adminShop'
import { ElMessage } from 'element-plus'

const activeTab = ref('products')
const loading = ref(false)
const productList = ref([])
const dialogVisible = ref(false)
const dialogType = ref('add')
const productForm = reactive({
  id: null,
  name: '',
  price: 0,
  stock: 0,
  type: '',
  imageUrl: '',
  description: '',
  days: 0,
  videoUrl: ''
})

// Order Audit
const orderLoading = ref(false)
const orderList = ref([])
const orderTotal = ref(0)
const orderPage = ref(1)
const orderPageSize = ref(10)
const orderSearch = reactive({
  userId: '',
  orderId: ''
})

const shipDialogVisible = ref(false)
const shipForm = reactive({
    id: null,
    trackingNumber: ''
})

const formatDate = (_row: any, _column: any, value: string) => {
    if (!value) return ''
    const date = new Date(value)
    const pad = (n: number) => n < 10 ? `0${n}` : n
    return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())} ${pad(date.getHours())}:${pad(date.getMinutes())}:${pad(date.getSeconds())}`
}

const loadProducts = async () => {
  loading.value = true
  try {
    const res: any = await getAdminProducts({ page: 1, size: 100 }) // Load all for now or implement pagination
    productList.value = res.data.records
  } catch (error) {
    console.error('Failed to load products', error)
  } finally {
    loading.value = false
  }
}

const handleAddProduct = () => {
  dialogType.value = 'add'
  Object.assign(productForm, {
    id: null,
    name: '',
    price: 0,
    stock: 10,
    type: 'PHYSICAL',
    imageUrl: '',
    description: '',
    days: 0,
    videoUrl: ''
  })
  dialogVisible.value = true
}

const handleEditProduct = (row: any) => {
  dialogType.value = 'edit'
  Object.assign(productForm, row)
  dialogVisible.value = true
}

const handleDeleteProduct = async (id: number) => {
  try {
    await deleteProduct(id)
    ElMessage.success('删除成功')
    loadProducts()
  } catch (error: any) {
    ElMessage.error(error.message || '删除失败')
  }
}

const uploadHeaders = {
    Authorization: `Bearer ${localStorage.getItem('token')}`
}

const handleAvatarSuccess = (response: any, _uploadFile: any) => {
    if (response.code === 200) {
        // Backend returns relative path "/uploads/..."
        // If needed, prepend base URL or handle static serving configuration
        // Assuming backend serves static files at root or configured properly
        productForm.imageUrl = response.data
        ElMessage.success('上传成功')
    } else {
        ElMessage.error(response.message || '上传失败')
    }
}

const beforeAvatarUpload = (rawFile: any) => {
    if (rawFile.type !== 'image/jpeg' && rawFile.type !== 'image/png') {
        ElMessage.error('Avatar picture must be JPG format!')
        return false
    } else if (rawFile.size / 1024 / 1024 > 2) {
        ElMessage.error('Avatar picture size can not exceed 2MB!')
        return false
    }
    return true
}

const submitProduct = async () => {
  if (!productForm.name || !productForm.price) {
    ElMessage.warning('请填写必填项')
    return
  }
  try {
    await saveProduct(productForm)
    ElMessage.success(dialogType.value === 'add' ? '添加成功' : '修改成功')
    dialogVisible.value = false
    loadProducts()
  } catch (error: any) {
    ElMessage.error(error.message || '保存失败')
  }
}

// Order Audit Functions
const loadOrders = async () => {
  orderLoading.value = true
  try {
    const params = {
      page: orderPage.value,
      size: orderPageSize.value,
      userId: orderSearch.userId || undefined,
      orderId: orderSearch.orderId || undefined
    }
    const res: any = await getOrders(params)
    orderList.value = res.data.records
    orderTotal.value = res.data.total
  } catch (e) {} finally {
    orderLoading.value = false
  }
}

const handleOrderPageChange = (page: number) => {
  orderPage.value = page
  loadOrders()
}

const handleRefund = async (id: number) => {
  try {
    await refundOrder(id)
    ElMessage.success('退款成功')
    loadOrders()
  } catch (e: any) {
    ElMessage.error(e.message || '操作失败')
  }
}

const handleMarkAbnormal = async (id: number) => {
  try {
    await markOrderAbnormal(id)
    ElMessage.success('已标记为异常')
    loadOrders()
  } catch (e: any) {
    ElMessage.error(e.message || '操作失败')
  }
}

const handleCancelAbnormal = async (id: number) => {
  try {
    await cancelOrderAbnormal(id)
    ElMessage.success('已取消异常状态')
    loadOrders()
  } catch (e: any) {
    ElMessage.error(e.message || '操作失败')
  }
}

const handleShip = (row: any) => {
    shipForm.id = row.id
    shipForm.trackingNumber = ''
    shipDialogVisible.value = true
}

const submitShip = async () => {
    if (!shipForm.trackingNumber) {
        ElMessage.warning('请输入快递单号')
        return
    }
    try {
        await shipOrder(shipForm.id!, shipForm.trackingNumber)
        ElMessage.success('发货成功')
        shipDialogVisible.value = false
        loadOrders()
    } catch (e: any) {
        ElMessage.error(e.message || '发货失败')
    }
}

onMounted(() => {
    loadProducts()
    loadOrders()
})
</script>

<style scoped>
.avatar-uploader .el-upload {
  border: 1px dashed var(--el-border-color);
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  transition: var(--el-transition-duration-fast);
}

.avatar-uploader .el-upload:hover {
  border-color: var(--el-color-primary);
}

.avatar-uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 178px;
  height: 178px;
  text-align: center;
  line-height: 178px; /* Center icon vertically */
  display: flex;
  justify-content: center;
  align-items: center;
}

.avatar {
  width: 178px;
  height: 178px;
  display: block;
}
</style>
