package com.vertigo.usersmanager.service;

import com.vertigo.usersmanager.dto.ReqRes;
import com.vertigo.usersmanager.entity.OurUsers;
import com.vertigo.usersmanager.repository.UsersRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;

@Service
public class UsersManagementService {

    @Autowired
    private UsersRepo usersRepo;
    @Autowired
    private JWTUtils jwtUtils;
    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private PasswordEncoder passwordEncoder;

    public ReqRes register(ReqRes registrationRequest){
        ReqRes resp = new ReqRes();

        try{
            OurUsers ourUser = new OurUsers();
            ourUser.setEmail(registrationRequest.getEmail());
            ourUser.setCity(registrationRequest.getCity());
            ourUser.setRole(registrationRequest.getRole());
            ourUser.setName(registrationRequest.getName());
            ourUser.setPassword(passwordEncoder.encode(registrationRequest.getPassword()));
            OurUsers ourUsersResult = usersRepo.save(ourUser);
            if(ourUsersResult.getId()>0) {
              resp.setOurUsers((ourUsersResult));
                resp.setMessage("User Saved Successfully");
                resp.setStatusCode(200);
            }
        }catch(Exception e){
        resp.setStatusCode(500);
        resp.setError(e.getMessage());
    }
        return resp;
    }

    public ReqRes login(ReqRes loginRequest){
        ReqRes response = new ReqRes();
        try{
            authenticationManager
                    .authenticate(new UsernamePasswordAuthenticationToken(loginRequest.getEmail(),
                            loginRequest.getPassword()));
            var user = usersRepo.findByEmail(loginRequest.getEmail()).orElseThrow();
            var jwt = jwtUtils.generateToken(user);
            var refreshToken = jwtUtils.generateRefreshToken(new HashMap<>(), user);
            response.setStatusCode(200);
            response.setToken(jwt);
            response.setRole(user.getRole());
            response.setRefreshToken(refreshToken);
            response.setExpirationTime("24Hrs");
            response.setMessage("Succesful Logged In");

        }catch (Exception e){
            response.setStatusCode(500);
            response.setMessage(e.getMessage());
        }
        return  response;
    }

    public ReqRes refreshToken(ReqRes refreshTokenRequest){
        ReqRes response = new ReqRes();
        try{
            String ourEmail = jwtUtils.extractUsername(refreshTokenRequest.getToken());
            OurUsers users = usersRepo.findByEmail(ourEmail).orElseThrow();
            if (jwtUtils.isTokenValid(refreshTokenRequest.getToken(), users)) {
                var jwt = jwtUtils.generateToken(users);
                response.setStatusCode(200);
                response.setToken(jwt);
                response.setRefreshToken(refreshTokenRequest.getToken());
                response.setExpirationTime("24Hrs");
                response.setMessage("Succesfully Refresh Token");
            }
        response.setStatusCode(200);
            return response;
        }catch (Exception e){
            response.setStatusCode(500);
            response.setError(e.getMessage());
            return response;
        }
    }

    public ReqRes getAllUsers(){
        ReqRes reqRes = new ReqRes();

        try {
            List<OurUsers> result = usersRepo.findAll();
            if(!result.isEmpty()) {
                reqRes.setOurUsersList(result);
                reqRes.setStatusCode(200);
                reqRes.setMessage("Succesful");
            }else{
                reqRes.setStatusCode(404);
                reqRes.setMessage("Not Found");
            }
            return reqRes;
        }catch (Exception e){
            reqRes.setStatusCode(500);
            reqRes.setMessage("Error occured:" +e.getMessage());
            return reqRes;
        }
    }
    public ReqRes getUserById(int id){
        ReqRes reqRes = new ReqRes();
        try {
            OurUsers usersById = usersRepo.findById(id).orElseThrow(()-> new RuntimeException("Users Not fount"));
            reqRes.setOurUsers(usersById);
            reqRes.setStatusCode(200);
            reqRes.setMessage("Users with id " + id + " found successfully");
        }catch (Exception e){
            reqRes.setStatusCode(500);
            reqRes.setMessage("Error occured:" +e.getMessage());
        }
        return reqRes;
    }

    public ReqRes deleteUser(int UserId){
        ReqRes reqRes = new ReqRes();
        try {
            Optional<OurUsers> usersOptional = usersRepo.findById(UserId);
            if (usersOptional.isPresent()) {
                usersRepo.deleteById(UserId);
                reqRes.setStatusCode(200);
                reqRes.setMessage("User Deleted Successfully");
            }else {
                reqRes.setStatusCode(404);
                reqRes.setMessage("User not found for deletion");
            }
        }catch (Exception e){
            reqRes.setStatusCode(500);
            reqRes.setMessage("Error occured while deleting user:" +e.getMessage());
        }
        return reqRes;
    }
    public ReqRes updateUser(Integer userId, OurUsers updatedUsers){
        ReqRes reqRes = new ReqRes();
        try {
            Optional<OurUsers> usersOptional = usersRepo.findById(userId);
            if (usersOptional.isPresent()) {
                OurUsers existingUser = usersOptional.get();
                existingUser.setEmail(updatedUsers.getEmail());
                existingUser.setName(updatedUsers.getName());
                existingUser.setCity(updatedUsers.getCity());
                existingUser.setRole(updatedUsers.getRole());

                //Check if password is present in the request
                if( updatedUsers.getPassword() != null && !updatedUsers.getPassword().isEmpty() ){
                    existingUser.setPassword(passwordEncoder.encode(updatedUsers.getPassword()));
                }
                OurUsers savedUser = usersRepo.save(existingUser);
                reqRes.setOurUsers(savedUser);
                reqRes.setStatusCode(200);
                reqRes.setMessage("User Updated Successfully");
            }else {
                reqRes.setStatusCode(404);
                reqRes.setMessage("User not found for update");
            }
        }catch (Exception e){
            reqRes.setStatusCode(500);
            reqRes.setMessage("Error occured while updating user:" +e.getMessage());
        }
        return reqRes;
    }

    public ReqRes getMyInfo(String email){
        ReqRes reqRes = new ReqRes();
        try {
            Optional<OurUsers> usersOptional = usersRepo.findByEmail(email);
            if (usersOptional.isPresent()) {
                reqRes.setOurUsers(usersOptional.get());
                reqRes.setStatusCode(200);
                reqRes.setMessage("User Found");
            }else {
                reqRes.setStatusCode(404);
                reqRes.setMessage("User Not Found for update");
            }

        }catch (Exception e){
            reqRes.setStatusCode(500);
            reqRes.setMessage("Error occured while getting user info:" +e.getMessage());
        }
        return reqRes;
    }












}
