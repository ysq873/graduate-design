<template>
  <a-card :bordered="false">
    <a-card
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
        添加
      </template>
      <a-card-meta
        title="无忧代理"
        description="价格实惠、服务一流、质量上乘、技术支持"
      >
      </a-card-meta>
    </a-card>

    <a-modal
      title="创建代理"
      width="50%"
      @ok="doCreateProxy"
      :maskClosable="false"
      :visible="proxyDialog"
      @cancel="proxyDialog = false"
    >
      <a-form :form="proxyForm">
        <a-form-item
          label="代理别名"
          :label-col="{ span: 8 }"
          :wrapper-col="{ span: 12 }"
        >
          <a-input placeholder="请给你的代理起个别名" v-decorator="['alias', {rules: [{ required: true, message: '请给你的代理起个别名' }], initialValue: proxyForm.alias}]" />
        </a-form-item>
        <a-form-item
          label="IP提取码"
          :label-col="{ span: 8 }"
          :wrapper-col="{ span: 12 }"
        >
          <a-input placeholder="请输入IP提取码" v-decorator="['token', {rules: [{ required: true, message: '请输入IP提取码' }], initialValue: proxyForm.token}]" />
        </a-form-item>

        <a
          target="_blank"
          href="http://www.data5u.com/"
        >打开官网</a>
      </a-form>
    </a-modal>
  </a-card>
</template>

<script>
import _ from "lodash";
import { addProxy } from "@/api/proxy";

export default {
  name: "createProxy",

  data() {
    return {
      proxyForm: this.$form.createForm(this),
      proxyDialog: false,

      code: "data5u"
    };
  },

  mounted() {},

  methods: {
    createProxy(code) {
      this.proxyDialog = true;
      this.code = code;
    },

    doCreateProxy() {
      this.proxyForm.validateFields((errors, values) => {
        if (!errors) {
          let form = {
            alias: values.alias,
            code: this.code,
            token: values.token,
          };
          addProxy(form)
            .then((data) => {
              if (data.success) {
                this.$message.success("创建成功");
                this.proxyDialog = false;
              }
            })
            .catch((err) => {
              console.log(err);
            });
        }
      });
    },
  },
};
</script>
