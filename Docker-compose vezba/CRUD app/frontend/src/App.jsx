import React, { useEffect, useState } from 'react';
import './App.css';

function App() {
  const [employees, setEmployees] = useState([]);
  const [loading, setLoading] = useState(true);

  // Form state
  const [name, setName] = useState('');
  const [email, setEmail] = useState('');
  const [phone, setPhone] = useState('');
  const [salary, setSalary] = useState('');

  // Editing state
  const [isEditing, setIsEditing] = useState(false);
  const [editingId, setEditingId] = useState(null);

  // Fetch all employees on mount
  useEffect(() => {
    fetchEmployees();
  }, []);

  const fetchEmployees = () => {
    fetch('http://localhost:5000/api/employees')
      .then(res => {
        if (!res.ok) throw new Error('Greška prilikom učitavanja zaposlenih');
        return res.json();
      })
      .then(data => {
        setEmployees(data);
        setLoading(false);
      })
      .catch(error => {
        console.error('Greška:', error);
        setLoading(false);
      });
  };

  // Handle form submit for add or edit
  const handleSubmit = (e) => {
    e.preventDefault();

    const employeeData = { name, email, phone, salary: Number(salary) };

    if (isEditing) {
      // Update existing employee
      fetch(`http://localhost:5000/api/employees/${editingId}`, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(employeeData),
      })
        .then(res => {
          if (!res.ok) throw new Error('Greška prilikom izmene zaposlenog');
          return res.json();
        })
        .then(updatedEmployee => {
          setEmployees(employees.map(emp => (emp.id === editingId ? updatedEmployee : emp)));
          resetForm();
        })
        .catch(err => console.error('Greška:', err));
    } else {
      // Add new employee
      fetch('http://localhost:5000/api/employees', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(employeeData),
      })
        .then(res => {
          if (!res.ok) throw new Error('Greška prilikom dodavanja zaposlenog');
          return res.json();
        })
        .then(newEmployee => {
          setEmployees([...employees, newEmployee]);
          resetForm();
        })
        .catch(err => console.error('Greška:', err));
    }
  };

  // Start editing - populate form with employee data
  const startEditing = (emp) => {
    setIsEditing(true);
    setEditingId(emp.id);
    setName(emp.name);
    setEmail(emp.email);
    setPhone(emp.phone);
    setSalary(emp.salary);
  };

  // Cancel editing and reset form
  const resetForm = () => {
    setIsEditing(false);
    setEditingId(null);
    setName('');
    setEmail('');
    setPhone('');
    setSalary('');
  };

  // Handle deleting employee
  const handleDelete = (id) => {
    if (!window.confirm('Da li ste sigurni da želite da obrišete ovog zaposlenog?')) {
      return;
    }

    fetch(`http://localhost:5000/api/employees/${id}`, {
      method: 'DELETE',
    })
      .then(res => {
        if (!res.ok) throw new Error('Greška pri brisanju zaposlenog');
        setEmployees(employees.filter(emp => emp.id !== id));
      })
      .catch(err => console.error('Greška:', err));
  };

  return (
    <div style={{ padding: '2rem', fontFamily: 'Arial' }}>
      <h1>Zaposleni</h1>
      {loading ? (
        <p>Učitavanje...</p>
      ) : (
        <>
          <table border="1" cellPadding="10" cellSpacing="0">
            <thead>
              <tr>
                <th>Ime i prezime</th>
                <th>Email</th>
                <th>Telefon</th>
                <th>Plata (RSD)</th>
                <th>Akcija</th>
              </tr>
            </thead>
            <tbody>
              {employees.map(emp => (
                <tr key={emp.id}>
                  <td>{emp.name}</td>
                  <td>{emp.email}</td>
                  <td>{emp.phone}</td>
                  <td>{emp.salary}</td>
                  <td>
                    <button onClick={() => startEditing(emp)}>Izmeni</button>
                    <button
                      onClick={() => handleDelete(emp.id)}
                      style={{ marginLeft: '0.5rem', backgroundColor: 'red', color: 'white' }}
                    >
                      Obriši
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>

          <h2 style={{ marginTop: '2rem' }}>
            {isEditing ? 'Izmeni zaposlenog' : 'Dodaj novog zaposlenog'}
          </h2>

          <form onSubmit={handleSubmit}>
            <input
              type="text"
              placeholder="Ime"
              value={name}
              onChange={(e) => setName(e.target.value)}
              required
              style={{ marginBottom: '0.5rem', display: 'block', width: '300px' }}
            />
            <input
              type="email"
              placeholder="Email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              required
              style={{ marginBottom: '0.5rem', display: 'block', width: '300px' }}
            />
            <input
              type="text"
              placeholder="Telefon"
              value={phone}
              onChange={(e) => setPhone(e.target.value)}
              required
              style={{ marginBottom: '0.5rem', display: 'block', width: '300px' }}
            />
            <input
              type="number"
              placeholder="Plata"
              value={salary}
              onChange={(e) => setSalary(e.target.value)}
              required
              style={{ marginBottom: '0.5rem', display: 'block', width: '300px' }}
            />

            <button type="submit" style={{ padding: '0.5rem 1rem' }}>
              {isEditing ? 'Sačuvaj izmene' : 'Dodaj'}
            </button>

            {isEditing && (
              <button
                type="button"
                onClick={resetForm}
                style={{ marginLeft: '1rem', padding: '0.5rem 1rem' }}
              >
                Otkaži
              </button>
            )}
          </form>
        </>
      )}
    </div>
  );
}

export default App;