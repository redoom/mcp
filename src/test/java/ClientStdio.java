import com.example.McpServerApplication;
import com.example.mcp.model.DataLabel;
import com.example.mcp.repository.VvtrData;
import com.example.mcp.tool.Vvtr;
import com.example.mcp.util.CsvMerger;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

@SpringBootTest(classes = McpServerApplication.class)
class ClientStdio {

    @Resource
    private Vvtr vvtr;
    @Resource
    private VvtrData vvtrData;

    @Test
    public void testVvtr() throws Exception {
        List<String> paths = vvtr.getFundData("fund", "1d", "501095", "", "");
        System.out.println(paths);
        System.out.printf(vvtr.getDayData(paths, "501095", 0, 1000));
        System.out.println(vvtr.getDataCount(paths, "1d", "501095"));
        List<Path> path = paths.stream()
                .map(Paths::get)
                .collect(Collectors.toList());
        System.out.println(vvtrData.testData(path, 8));

    }

}
