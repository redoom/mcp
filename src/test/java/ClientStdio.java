import com.example.McpServerApplication;
import com.example.mcp.model.DataBack;
import com.example.mcp.model.DataLabel;
import com.example.mcp.repository.VvtrData;
import com.example.mcp.tool.Vvtr;
import com.example.mcp.util.CsvMerger;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.shadow.com.univocity.parsers.csv.Csv;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
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

    @Test
    public void testFilterData() throws Exception {
        List<String> fundData = vvtr.getFundData("fund", "1m", "159206", "", "");
        List<Path> paths = fundData.stream()
                            .map(Paths::get)
                            .toList();
        System.out.println();
        DataBack minData = vvtrData.getMinData(paths, "2025-04-01 09:36:00", "2025-04-01 09:55:00", 8);
        System.out.println(minData.getData());
    }

}
