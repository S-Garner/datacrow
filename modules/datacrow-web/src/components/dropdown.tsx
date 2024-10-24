import  { useState } from "react";

type DropdownProps = {
  title: string;
  options: number[];
  handleSelectOption: (option: string) => void;
};

export default function Dropdown({
  title,
  options,
  handleSelectOption,
}: DropdownProps) {
  const [isOpen, setIsOpen] = useState(false);

  const toggleDropdown = () => {
    setIsOpen(!isOpen);
  };

  return (
    <div className="relative">
      {/* Trigger */}
      <button
        className="bg-gray-200 text-gray-700 font-semibold py-2 px-4 rounded inline-flex items-center w-auto"
        onClick={toggleDropdown}
      >
        <span>{title}</span>
        <svg
          className={`fill-current h-4 w-4 ml-2 ${
            isOpen ? "transform rotate-180" : ""
          }`}
          xmlns="http://www.w3.org/2000/svg"
          viewBox="0 0 20 20"
        >
          <path d="M10 12l-6-6h12z" />
        </svg>
      </button>

      {/* Dropdown menu */}
      {isOpen && (
        <ul className="absolute text-gray-700 pt-1 bg-white border border-gray-300 rounded mt-1 w-44">
          {options.map((option) => (
            <li
              key={option}
              className="rounded-t bg-gray-200 hover:bg-gray-400 py-2 px-4 block whitespace-no-wrap"
              onClick={() => {
                handleSelectOption("" + option);
                setIsOpen(false);
              }}
            >
              {option}
            </li>
          ))}
        </ul>
      )}
    </div>
  );
}
