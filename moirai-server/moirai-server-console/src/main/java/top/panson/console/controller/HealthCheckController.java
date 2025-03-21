package top.panson.console.controller;

import top.panson.common.constant.Constants;
import top.panson.common.web.base.Result;
import top.panson.common.web.base.Results;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static top.panson.common.constant.Constants.UP;



/**
 * @方法描述：健康检查器，这个控制器专门接收客户端的健康检查请求，其实就是心跳请求
 */
@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping(Constants.BASE_PATH + "/health/check")
public class HealthCheckController {

    @GetMapping
    public Result<String> healthCheck() {
        return Results.success(UP);
    }
}