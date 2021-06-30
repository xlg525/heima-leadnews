package com.heima.wemedia.gateway.filter;

import com.heima.wemedia.gateway.utils.AppJwtUtil;
import io.jsonwebtoken.Claims;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

//全局过滤器
@Component
@Log4j2
public class AuthorizeFilter implements GlobalFilter, Ordered {
    //spring cloud gateway 底层基于webflux,基于reactor响应式编程模型
    //和servlet区别巨大的
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        //1.获取请求对象和响应对象
        ServerHttpRequest request = exchange.getRequest();
        ServerHttpResponse response = exchange.getResponse();
        //2.判断当前的请求是否为登录，如果是，直接放行 /admin/login/in
        if(request.getURI().getPath().contains("/login/in")){
            //放行 使用责任链设计模式，将当前请求传递给下一个过滤器
            return chain.filter(exchange);
        }
        //3.获取当前用户的请求头jwt信息  ?token=abc&token=def
        HttpHeaders headers = request.getHeaders();
        String jwtToken = headers.getFirst("token");
        //4.判断当前令牌是否存在
        if(StringUtils.isEmpty(jwtToken)){
            //如果不存在，向客户端返回错误提示信息
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            //终止请求，返回前端401
            return response.setComplete();
        }
        try {
            //5.如果令牌存在，解析jwt令牌，判断该令牌是否合法，如果不合法，则向客户端返回错误信息
            Claims claims = AppJwtUtil.getClaimsBody(jwtToken);
            int result = AppJwtUtil.verifyToken(claims);
            if(result == 0 || result == -1){
                //5.1 合法，则向header中重新设置userId
                Integer id = (Integer) claims.get("id");
                log.info("find userid:{} from uri:{}",id,request.getURI());
                //重新设置token到header中
                //spring cloud gateway的request不能直接设置值，需要先进行一步转化mutate()
                //然后再给头信息设置对应的数据
                ServerHttpRequest serverHttpRequest = request.mutate().headers(httpHeaders -> {
                    httpHeaders.add("userId", id + "");
                }).build();
                //同样请求也需要转化一下
                exchange.mutate().request(serverHttpRequest).build();
                if(result == 0){
                    //续约token 重新生成token 通过response返回给前端
                }
            }else{
                //校验失败，返回401
                response.setStatusCode(HttpStatus.UNAUTHORIZED);
                return response.setComplete();
            }
        }catch (Exception e){
            e.printStackTrace();
            //想客户端返回错误提示信息
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return response.setComplete();
        }
        //6.放行
        return chain.filter(exchange);
    }
    /**
     * 优先级设置
     * 多个过滤器
     * 值越小，优先级越高
     * @return
     */
    @Override
    public int getOrder() {
        return 0;
    }
}
