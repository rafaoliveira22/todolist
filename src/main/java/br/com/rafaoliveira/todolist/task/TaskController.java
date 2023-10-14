package br.com.rafaoliveira.todolist.task;

import br.com.rafaoliveira.todolist.utils.Utils;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/tasks")
public class TaskController {
    @Autowired
    private ITaskRepository taskRepository;

    @PostMapping("/")
    public ResponseEntity create(@RequestBody TaskModel taskModel, HttpServletRequest request){
        var userId = request.getAttribute("userId");
        taskModel.setUserId((UUID)userId);

        var currentDate = LocalDateTime.now();
        if(currentDate.isAfter((taskModel.getStartAt())) || currentDate.isAfter((taskModel.getEndAt()))){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("A data de início/término deve ser maior que data atual");
        }

        if(taskModel.getStartAt().isAfter(taskModel.getEndAt())){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("A data de início deve ser menor do que a data de término");
        }

        var task = this.taskRepository.save(taskModel);
        return ResponseEntity.status(HttpStatus.CREATED).body(task);
    } //create()

    @GetMapping("/")
    public List<TaskModel> list(HttpServletRequest request){
        var userId = request.getAttribute("userId");
        var tasks = this.taskRepository.findByUserId((UUID) userId);
        return tasks;
    } //list()

    @PutMapping("/{taskId}")
    public ResponseEntity update(@RequestBody TaskModel taskModel, @PathVariable UUID taskId, HttpServletRequest request){
        var task = this.taskRepository.findById(taskId).orElse(null);
        if(task == null){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(String.format("%s : tarefa não encontrada.", taskId));
        }
        var userId = request.getAttribute("userId");

        if(!task.getUserId().equals(userId)){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Usuário sem permissão para alterar essa tarefa");
        }

        Utils.copyNonNullProperties(taskModel, task);
        var taskUpdated =  this.taskRepository.save(task);
        return ResponseEntity.status(HttpStatus.CREATED).body(taskUpdated);
    } //update
} //TaskController{}
