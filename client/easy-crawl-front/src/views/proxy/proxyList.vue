<template>
  <a-card
    :bordered="false"
    :loading="loading"
  >

    <div class="proxy-list">
      <a-card
        class="proxy"
        v-for="(item) of proxies"
        :key="item.id"
        hoverable
        style="width: 300px"
        @click="createProxy('data5u')"
      >
        <img
          slot="cover"
          alt="example"
          src="../../assets/proxy/data5u.jpg"
        />
        <template
          slot="actions"
          class="ant-card-actions"
        >

        </template>
        <a-card-meta
          :title="item.alias"
          :description="'IP提取码：' + item.token"
        >
        </a-card-meta>
      </a-card>
    </div>

  </a-card>
</template>

<script>
import _ from "lodash";
import { listProxy } from "@/api/proxy";

export default {
  name: "createProxy",

  data() {
    return {
      loading: false,
      proxies: [],
    };
  },

  mounted() {
    this.listProxy();
  },

  methods: {
    listProxy() {
      this.loading = true;
      listProxy({}).then((resp) => {
        if (resp.success) {
          this.proxies = resp.data;
          this.loading = false;
        }
      });
    },
  },
};
</script>

<style lang="less" >
.proxy-list {
  .proxy {
    display: inline-block;
    margin-right: 16px;
    margin-bottom: 16px;
  }
}
</style>
