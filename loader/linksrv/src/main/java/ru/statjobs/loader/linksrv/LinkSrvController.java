package ru.statjobs.loader.linksrv;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import ru.statjobs.loader.common.dao.DownloadableLinkDao;
import ru.statjobs.loader.common.dto.DownloadableLink;

import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Controller
@RequestMapping("/linksrv")
public class LinkSrvController implements DownloadableLinkDao {

    @Override
    @PostMapping(value = "/create")
    @ResponseBody
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public boolean createDownloadableLink(@RequestBody DownloadableLink link) {
        return false;
    }

    @Override
    @RequestMapping(value = "/delete", method = RequestMethod.POST)
    @ResponseBody
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public boolean deleteDownloadableLink(DownloadableLink link) {
        return false;
    }

    @Override
    @RequestMapping(value = "/get", method = RequestMethod.GET)
    @ResponseBody
    @Produces(MediaType.APPLICATION_JSON)
    public DownloadableLink getDownloadableLink() {
        return null;
    }


}
