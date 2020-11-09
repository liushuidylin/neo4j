package cn.golaxy;

import org.neo4j.procedure.Description;
import org.neo4j.procedure.Name;
import org.neo4j.procedure.UserFunction;

/**
 * @Author: duyilin@golaxy.cn
 * @Date: Created in 11:36 2020/9/23
 */
public class UserFunctions {
    @UserFunction("my.greet")
    @Description("问候")
    public String greet(@Name("name") String name) {
        return "Hello "+name;
    }
}
