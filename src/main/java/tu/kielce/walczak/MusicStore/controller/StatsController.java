package tu.kielce.walczak.MusicStore.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import tu.kielce.walczak.MusicStore.service.StatsService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/stats")
public class StatsController {
    private StatsService statsService;

    @Autowired
    public StatsController(StatsService statsService) {
        this.statsService = statsService;
    }

    @GetMapping("salesByMonth")
    public List<Long> getSalesByMonth(@RequestParam("year") int year){
        System.out.println("Prośba o sprzedaż według miesięcy dla roku " + year);
        return statsService.getSalesByMonth(year);
    }

    @GetMapping("profitsByMonth")
    public List<Double> getProfitsByMonth(@RequestParam("year") int year){
        System.out.println("Prośba o dochody według miesięcy dla roku " + year);
        return statsService.getProfitsByMonth(year);
    }
}
