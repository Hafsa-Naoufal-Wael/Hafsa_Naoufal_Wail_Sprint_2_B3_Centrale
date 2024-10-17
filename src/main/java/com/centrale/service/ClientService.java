package com.centrale.service;

import java.util.List;
import java.util.Optional;

import com.centrale.model.entity.Client;
import com.centrale.model.entity.User;
import com.centrale.repository.ClientRepository;

public class ClientService {

    private final ClientRepository clientRepository;

    public ClientService(ClientRepository clientRepository) {
        this.clientRepository = clientRepository;
    }

    public Client saveClient(Client client) {
        return clientRepository.save(client);
    }
    public void updateClient(Client client) {
        clientRepository.updateClient(client);
    }
    public Optional<Client> getClientById(Long id) {
        return clientRepository.findById(id);
    }


    public List<Client> getAllClients() {
        return clientRepository.findAll();
    }

    public void deleteClient(Client client) {
        clientRepository.delete(client);
    }
    public List<Client> getAllClientsPaginated(int page, int size) {
        return clientRepository.findAllPaginated(page, size);
    }
    public Client findByUser(User user) {
        return clientRepository.findByUser(user);
    }
    public Client getClientByEmail(String email) {
        return clientRepository.findByEmail(email);
    }
    public Client createClient(Client client) {
        return clientRepository.save(client);
    }
    
}

