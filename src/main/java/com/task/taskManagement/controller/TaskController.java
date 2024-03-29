package com.task.taskManagement.controller;

import com.task.taskManagement.entities.Task;
import com.task.taskManagement.service.ServiceTask;
import com.task.taskManagement.service.ServiceTaskStatus;
import com.task.taskManagement.service.ServiceParticipant;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;

import lombok.AllArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;


@Controller
@AllArgsConstructor
public class TaskController {


    ServiceTask serviceTask;
    ServiceTaskStatus serviceTaskStatus;
    ServiceParticipant serviceParticipant;

    @GetMapping("/admin/tasks")
    public String getAllTasks (Model m,
                               @RequestParam(name = "mc", defaultValue = "") String mc,
                               @RequestParam(name = "page", defaultValue = "0") int page,
                               @RequestParam(name = "size", defaultValue = "6") int size)
    {
        page = Math.max(page, 0);
        Page<Task> tasks=serviceTask.getTaskByMc(mc, PageRequest.of(page, size));
        m.addAttribute("mc",mc);
        m.addAttribute("tasks", tasks.getContent());
        m.addAttribute("pages", new int[tasks.getTotalPages()]);
        m.addAttribute("currentpage", tasks.getNumber());


        return "task/tasks";
    }

    @GetMapping("/admin/addTask")
    public String addTask(Model m)
    {

        m.addAttribute("task", new Task());
        return "task/addTask";
    }

    @PostMapping("/admin/addTask")
    public String saveTask(@Valid Task t, BindingResult bindingResult, Model m,
                           @RequestParam("pdfFile") MultipartFile mf) throws IOException {
        if (bindingResult.hasErrors()) {
            return "task/addTask";
        }

        serviceTask.saveTask(t, mf);
        return "redirect:/admin/tasks";
    }

    @GetMapping("/admin/task/{id}")
    public String getTask(@PathVariable("id") Long id, Model m) {
        Task task = serviceTask.getTask(id);
        m.addAttribute("task", task);
        return "task/viewTask";
    }

    @GetMapping("/admin/edit/task/{id}")
    public String editTask(@PathVariable("id") Long id, Model model ) {
        Task task = serviceTask.getTask(id);
        model.addAttribute("task", task);
        return "task/editTask";
    }

    @PostMapping("/admin/edit/task/{id}")
    public String editTask(@PathVariable("id") Long id, @Valid Task editedTask, BindingResult bindingResult,
                           @RequestParam("pdfFile") MultipartFile mf) throws IOException {
        if (bindingResult.hasErrors()) {
            return "task/editTask";
        }

        serviceTask.editTask(id, editedTask, mf);
        return "redirect:/admin/tasks";
    }

    @GetMapping("/admin/delete/task/{id}")
    public String deleteTask(@PathVariable("id") Long idTask)
    {
        serviceTask.deleteTask(idTask);
        return "redirect:/admin/tasks";
    }

    @GetMapping("/admin/downloadFile")
    public ResponseEntity<Resource> downloadFile(@RequestParam String fileName) throws IOException {
        Resource resource = new ClassPathResource("static/photos/" + fileName);

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName);

        return ResponseEntity.ok()
                .headers(headers)
                .contentLength(resource.getFile().length())
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(new InputStreamResource(resource.getInputStream()));
    }


    


    /*@GetMapping("/")
    public String home()
    {
        return "redirect:/tasks";
    }*/




}
