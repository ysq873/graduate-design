<template>
  <a-card :bordered="false">
    <div class="table-page-search-wrapper">
      <a-form layout="inline">
        <a-row :gutter="48">
          <a-col
            :md="8"
            :sm="24"
          >
            <a-form-item label="蜘蛛名">
              <a-input
                v-model="searchModel.spiderName"
                placeholder=""
              />
            </a-form-item>
          </a-col>

          <template v-if="advanced">
            <a-col
              :md="8"
              :sm="24"
            >
              <a-form-item label="表名">
                <a-input
                  v-model="searchModel.spiderTableName"
                  placeholder=""
                />
              </a-form-item>
            </a-col>
          </template>
          <a-col
            :md="!advanced && 8 || 24"
            :sm="24"
          >
            <span
              class="table-page-search-submitButtons"
              :style="advanced && { float: 'right', overflow: 'hidden' } || {} "
            >
              <a-button
                type="primary"
                @click="search"
              >查询</a-button>
              <a-button
                style="margin-left: 8px"
                @click="() => searchModel = {}"
              >重置</a-button>
              <a
                @click="toggleAdvanced"
                style="margin-left: 8px"
              >
                {{ advanced ? '收起' : '展开' }}
                <a-icon :type="advanced ? 'up' : 'down'" />
              </a>
            </span>
          </a-col>
        </a-row>
      </a-form>
    </div>

    <div class="table-operator">

    </div>

    <a-table
      :columns="columns"
      :rowKey="record => record.id"
      :dataSource="data"
      :pagination="pagination"
      :loading="loading"
      @change="handleTableChange"
    >
      <template
        slot="status"
        slot-scope="text"
      >
        <a-badge
          :status="text | statusTypeFilter"
          :text="text | statusFilter"
        />
      </template>

      <template
        slot="action"
        slot-scope="text, record"
      >
        <a-button
          class="m-r-8 m-b-8"
          type="primary"
          size="small"
          @click="seeResult(record)"
        >查看爬取结果</a-button>

        <a-popconfirm
          v-if="record.status !== 'success' || record.status === 'error'"
          title="确认结束?"
          @confirm="stopJob(record.id)"
          okText="确认"
          cancelText="取消"
        >
          <a-icon
            slot="icon"
            type="question-circle-o"
            style="color: red"
          />
          <a-button
            class="m-r-8 m-b-8"
            type="danger"
            size="small"
          >结束</a-button>
        </a-popconfirm>

        <a-popconfirm
          v-if="record.status === 'running'"
          title="确认暂停?"
          @confirm="pauseJob(record.id)"
          okText="确认"
          cancelText="取消"
        >
          <a-icon
            slot="icon"
            type="question-circle-o"
            style="color: red"
          />
          <a-button
            class="m-r-8 m-b-8"
            size="small"
          >暂停</a-button>
        </a-popconfirm>

        <a-popconfirm
          v-if="record.status === 'paused'"
          title="确认重启?"
          @confirm="restartJob(record.id)"
          okText="确认"
          cancelText="取消"
        >
          <a-icon
            slot="icon"
            type="question-circle-o"
            style="color: red"
          />
          <a-button
            class="m-r-8"
            size="small"
          >重启</a-button>
        </a-popconfirm>

      </template>
    </a-table>
  </a-card>
</template>

<script>
import moment from "moment";
import _ from "lodash";
import { Ellipsis } from "@/components";
import { searchJob } from "@/api/job";
import { stopJob, pauseJob, restartJob } from "@/api/job";
import { setTimeout } from "timers";

const statusMap = {
  wait: {
    status: "default",
    text: "待运行"
  },

  queuing: {
    status: "default",
    text: "排队中"
  },

  paused: {
    status: "default",
    text: "暂停中"
  },

  running: {
    status: "processing",
    text: "运行中"
  },
  success: {
    status: "success",
    text: "完成"
  },
  error: {
    status: "error",
    text: "异常"
  }
};

const defaultModel = {
  pageNo: 1,
  pageSize: 10,
  count: 0
};

const columns = [
  {
    title: "蜘蛛名",
    dataIndex: "spiderName"
  },
  {
    title: "表名",
    dataIndex: "spiderTableName"
  },
  {
    title: "状态",
    dataIndex: "status",
    width: 100,
    scopedSlots: { customRender: "status" }
  },
  {
    title: "开始时间",
    dataIndex: "startTime"
  },
  {
    title: "完成时间",
    dataIndex: "endTime"
  },
  {
    title: "耗时(秒)",
    dataIndex: "timeCost"
  },
  {
    title: "爬取数量",
    dataIndex: "successNum"
  },
  {
    title: "操作",
    dataIndex: "action",
    scopedSlots: { customRender: "action" }
  }
];
export default {
  name: "JobList",
  components: {
    Ellipsis
  },
  data() {
    return {
      // 高级搜索 展开/关闭
      advanced: true,
      // 查询参数
      searchModel: _.cloneDeep(defaultModel),
      data: [],
      columns,
      pagination: {},
      loading: false,
      opLoading: false
    };
  },

  filters: {
    statusFilter(type) {
      return statusMap[type].text;
    },
    statusTypeFilter(type) {
      return statusMap[type].status;
    }
  },

  mounted() {
    this.searchModel.spiderName = this.$route.query.spiderName;
    this.searchModel.spiderTableName = this.$route.query.spiderTableName;
    this.search();
  },

  methods: {
    pauseJob(id) {
      pauseJob(id).then(data => {
        if (data.success) {
          this.$message.success("暂停成功");
          this.search();
        }
      });
    },
    restartJob(id) {
      restartJob(id).then(data => {
        if (data.success) {
          this.$message.success("重启成功");
          this.search();
        }
      })
    },
    stopJob(id) {
      stopJob(id).then(data => {
        if (data.success) {
          this.$message.success("结束成功");
          this.search();
        }
      });
    },
    stopJob(id) {
      stopJob(id).then(data => {
        if (data.success) {
          this.$message.success("停止成功");
          this.search();
        }
      });
    },
    seeResult(record) {
      this.$router.push(`/spider/jobRecord/${record.id}`);
    },
    toggleAdvanced() {
      this.advanced = !this.advanced;
    },
    handleTableChange(pagination, filters, sorter) {
      console.log(pagination);
      const pager = { ...this.pagination };
      pager.current = pagination.current;
      this.pagination = pager;

      this.getList(pagination.current);
    },
    search() {
      this.getList(1);
    },
    getList(pageNo) {
      var self = this;
      this.loading = true;
      if (pageNo != undefined) {
        this.searchModel.pageNo = pageNo;
      }

      let searchModel = _.cloneDeep(this.searchModel);

      searchJob(searchModel).then(data => {
        if (data.success) {
          this.data = data.data;
          const pagination = { ...this.pagination };
          // Read total count from server
          pagination.total = data.count;
          this.pagination = pagination;
        }
        this.loading = false;
      });
    }
  }
};
</script>
