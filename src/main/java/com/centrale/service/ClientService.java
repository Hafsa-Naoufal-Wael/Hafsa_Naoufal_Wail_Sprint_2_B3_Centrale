package com.centrale.service;

import java.util.List;
import java.util.Optional;

import com.centrale.model.entity.Client;
import com.centrale.repository.ClientRepository;

public class ClientService {

    private final ClientRepository clientRepository;

    public ClientService(ClientRepository clientRepository) {
        this.clientRepository = clientRepository;
    }

    public Client saveClient(Client client) {
        return clientRepository.save(client);
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
}

