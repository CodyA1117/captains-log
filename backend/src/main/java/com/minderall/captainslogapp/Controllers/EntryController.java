package com.minderall.captainslogapp.Controllers;

import com.minderall.captainslogapp.Models.Entry;
import com.minderall.captainslogapp.Repositories.EntryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/logs")
public class EntryController {

    @Autowired
    private EntryRepository entryRepository;

    @GetMapping
    public List<Entry> getUserLogs(@RequestParam Long userId) {
        return entryRepository.findByUserId(userId);
    }
}
