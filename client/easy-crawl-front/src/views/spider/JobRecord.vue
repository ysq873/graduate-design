<template>
  <div>
    <a-card
      :bordered="false"
      style="margin-bottom:24px;"
      v-if='job && !(job.status === "success" ||
            job.status === "paused" ||
            job.status === "error")'
    >

      <div
        id="graphContainer"
        class="graph"
      ></div>
    </a-card>

    <a-card
      :bordered="false"
      style="margin-bottom:24px;"
    >
      <div class="toolbox">
        <div>
          任务状态：
          <span
            class="text-info"
            v-if="job.status === 'wait'"
          >待执行</span>
          <span
            class="text-info"
            v-if="job.status === 'queuing'"
          >排队中</span>
          <span
            class="text-primary"
            v-if="job.status === 'running'"
          >执行中</span>
          <span
            class="text-warning"
            v-if="job.status === 'paused'"
          >暂停</span>
          <span
            class="text-danger"
            v-if="job.status === 'error'"
          >失败</span>
          <span
            class="text-success"
            v-if="job.status === 'success'"
          >已完成</span>
        </div>

        <div>
          <a-popconfirm
            v-if="job.status === 'running'"
            title="确认暂停?"
            @confirm="pauseJob(job.id)"
            okText="确认"
            cancelText="取消"
          >
            <a-icon
              slot="icon"
              type="question-circle-o"
              style="color: red"
            />
            <a-button
              type="primary"
              class="m-r-8"
            >暂停</a-button>
          </a-popconfirm>

          <a-popconfirm
            v-if="job.status === 'paused'"
            title="确认重启?"
            @confirm="restartJob(job.id)"
            okText="确认"
            cancelText="取消"
          >
            <a-icon
              slot="icon"
              type="question-circle-o"
              style="color: red"
            />
            <a-button
              type="primary"
              class="m-r-8"
            >重启</a-button>
          </a-popconfirm>

          <a-popconfirm
            v-if="job.status !== 'success' || job.status === 'error'"
            title="确认结束?"
            @confirm="stopJob(job.id)"
            okText="确认"
            cancelText="取消"
          >
            <a-icon
              slot="icon"
              type="question-circle-o"
              style="color: red"
            />
            <a-button
              type="danger"
              class="m-r-8"
            >结束</a-button>
          </a-popconfirm>

           <a-button
              class="m-r-8"
              @click="exportExcel"
            >导出到Excel</a-button>
        </div>
      </div>

    </a-card>

    <a-card :bordered="false">

      <a-table
        :columns="columns"
        :rowKey="record => record.id ? record.id : record.ID"
        :dataSource="data"
        :pagination="pagination"
        :loading="loading"
        @change="handleTableChange"
      >
      </a-table>
    </a-card>

  </div>

</template>

<script>
import moment from "moment";
import $ from "jquery";
import _ from "lodash";
import { Ellipsis } from "@/components";
import {
  searchJobRecord,
  getStatInfo,
  getJob,
  stopJob,
  pauseJob,
  restartJob,
} from "@/api/job";
import { setTimeout } from "timers";

const defaultModel = {
  jobUuid: null,
  pageNo: 1,
  pageSize: 10,
  count: 0,
};

export default {
  name: "JobRecord",
  components: {
    Ellipsis,
  },
  data() {
    return {
      // 查询参数
      searchModel: _.cloneDeep(defaultModel),
      data: [],
      columns: [],
      pagination: {
        showTotal: (total) => `共${total}条`,
        position: "both",
      },
      loading: false,
      opLoading: false,

      myChart: null,

      job: {},

      timerId: null,
    };
  },

  mounted() {
    this.init();
  },

  methods: {
    exportExcel() {
      var $temp = $("<form type='hidden'>"); //创建form标签
      $temp.attr("target", "_black");
      $temp.attr("method", "get");
      $temp.attr("action", "/job-record/export-excel");//前端项目测试时加上/api
      var $input = $('<input type="hidden">');
      $input.attr("name", "jobUuid");
      $input.attr("value", this.searchModel.jobUuid);
      $temp.append($input);
      $(document.body).append($temp);
      $temp.submit();
    },

    init() {
      if (this.$route.params.jobUuid) {
        this.searchModel.jobUuid = this.$route.params.jobUuid;
      }
      this.search();

      getJob(this.searchModel.jobUuid).then((resp) => {
        if (resp.success) {
          this.job = resp.data;
          if (
            !(
              resp.data.status === "success" ||
              resp.data.status === "paused" ||
              resp.data.status === "error"
            )
          ) {
            setTimeout(() => {
              this.renderGraph();
            }, 1000);

            this.timerId = setInterval(() => {
              this.refreshData(this.searchModel.jobUuid);
            }, 2000);
          }
        }
      });
    },

    pauseJob(id) {
      pauseJob(id).then((data) => {
        if (data.success) {
          this.$message.success("暂停成功");
          clearInterval(this.timerId);
          this.init();
        }
      });
    },
    restartJob(id) {
      restartJob(id).then((data) => {
        if (data.success) {
          this.$message.success("重启成功");
          this.init();
        }
      });
    },
    stopJob(id) {
      stopJob(id).then((data) => {
        if (data.success) {
          this.$message.success("结束成功");
          clearInterval(this.timerId);
          this.init();
        }
      });
    },

    refreshData(id) {
      getStatInfo(id).then((resp) => {
        if (resp.success) {
          if (resp.data) {
            this.refreshGraph(resp.data.nodes);
          }
        } else {
          this.$message.error(resp.resultMessage);
        }
      });
      this.search();

      getJob(this.searchModel.jobUuid).then((resp) => {
        if (resp.success) {
          this.job = resp.data;
          if (
            resp.data.status === "success" ||
            resp.data.status === "paused" ||
            resp.data.status === "error"
          ) {
            //清除计时器
            clearInterval(this.timerId);
          }
        }
      });
    },

    refreshGraph(nodes) {
      if (!nodes || nodes.length === 0) {
        return;
      }

      let oldOption = this.myChart.getOption();
      oldOption.series = [
        {
          name: "每5秒爬取数",
          type: "line",
          smooth: true,
          data: nodes.map((x) => {
            return x.count;
          }),
        },
      ];

      oldOption.xAxis = {
        type: "category",
        boundaryGap: false,
        data: nodes.map((x) => {
          return x.time;
        }),
      };
      this.myChart.setOption(oldOption, true);
    },

    /**
     * 渲染图表
     */
    renderGraph(options) {
      let dom = document.getElementById("graphContainer");
      let myChart = echarts.init(dom);

      if (options) {
        myChart.setOption(options, true);
        return;
      }

      let option = {
        tooltip: {
          trigger: "axis",
        },
        legend: {
          data: ["每5秒爬取数"],
        },

        xAxis: {
          type: "category",
          boundaryGap: false,
          data: [],
        },
        yAxis: {
          type: "value",
          axisLabel: {
            formatter: "",
          },
        },
        series: [
          {
            name: "每5秒爬取数",
            type: "line",
            smooth: true,
            data: [],
          },
        ],
      };
      if (option && typeof option === "object") {
        myChart.setOption(option, true);
      }

      this.myChart = myChart;
    },

    handleTableChange(pagination, filters, sorter) {
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

      searchJobRecord(searchModel).then((data) => {
        if (data.success) {
          this.data = data.data;
          if (this.data.length > 0) {
            this.setColumns(this.data[0]);
          }
          const pagination = { ...this.pagination };
          // Read total count from server
          pagination.total = data.count;
          this.pagination = pagination;
        }
        this.loading = false;
      });
    },

    setColumns(data) {
      if (!data) {
        return;
      }

      if (this.columns.length) {
        return;
      }
      for (let field in data) {
        if (field.toLocaleLowerCase() === "job_uuid") {
          continue;
        }
        if (field === "id") {
          this.columns.unshift({ title: "id", dataIndex: "id" });
          continue;
        }
        if (field === "create_time") { 
          continue;
        }
        if (field === "modify_time") {
          continue;
        }
        let column = {
          title: field,
          dataIndex: field,
        };
        this.columns.push(column);
      }
      this.columns.push({ title: "create_time", dataIndex: "create_time" })
    },
  },
};
</script>

<style lang="less" scoped>
.graph {
  width: 100%;
  height: 300px;
}

.toolbox {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
