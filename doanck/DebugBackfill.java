import com.example.doanck.DoanckApplication;
import com.example.doanck.service.PendingTicketOrderService;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

public class DebugBackfill {
    public static void main(String[] args) {
        try (ConfigurableApplicationContext context = new SpringApplicationBuilder(DoanckApplication.class)
                .web(WebApplicationType.NONE)
                .run()) {
            PendingTicketOrderService service = context.getBean(PendingTicketOrderService.class);
            service.fulfillPendingOrdersForUser("giabao");
            System.out.println("backfill=DONE");
        }
    }
}
